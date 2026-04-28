package org.example.execution.executor;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import lombok.RequiredArgsConstructor;
import org.example.exception.DockerExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JavaExecutor implements CodeExecutor {

    private static final Pattern PUBLIC_CLASS_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z_$][A-Za-z\\d_$]*)");
    private static final String CONTAINER_WORK_DIR = "/workspace";
    private static final int MAX_DOCKER_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 200;

    private final DockerClient dockerClient;

    @Value("${application.execution.arena-image:codeguard-arena-java:latest}")
    private String arenaImage;

    @Value("${application.execution.cpu-set:0}")
    private String cpuSet;

    @Value("${application.execution.default-memory-mb:128}")
    private int defaultMemoryMb;

    // wrapper для перезапуска контейнера
    private <T> T withDockerRetry(Callable<T> action) {
        int attempt = 0;

        while (true) {
            try {
                return action.call();
            } catch (Exception ex) {
                attempt++;

                if (!isRetryableDockerError(ex) || attempt >= MAX_DOCKER_RETRIES) {
                    throw new DockerExecutionException(
                            "Docker operation failed after " + attempt + " attempts: " + ex.getMessage()
                    );
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    // проверка, является ли ошибка подходящей для перезапуска
    private boolean isRetryableDockerError(Exception ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        return msg.contains("connection reset") ||
                msg.contains("broken pipe") ||
                msg.contains("timeout") ||
                msg.contains("i/o error") ||
                msg.contains("temporarily unavailable") ||
                msg.contains("connection refused") ||
                msg.contains("context deadline exceeded");
    }

    @Override
    public ExecutionResult execute(String sourceCode, String inputData, int timeoutMs, int memoryMb) {
        String className = extractClassName(sourceCode);
        int memoryLimitMb = memoryMb > 0 ? memoryMb : defaultMemoryMb;
        long startedAt = System.currentTimeMillis();
        Path tempDir = null;
        String containerId = null;

        try {
            tempDir = Files.createTempDirectory("codeguard-java-run-");
            Files.writeString(tempDir.resolve(className + ".java"), sourceCode, StandardCharsets.UTF_8);
            Files.writeString(tempDir.resolve("input.txt"), inputData == null ? "" : inputData, StandardCharsets.UTF_8);

            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withAutoRemove(true)
                    .withMemory((long) memoryLimitMb * 1024 * 1024)
                    .withCpusetCpus(cpuSet)
                    .withNetworkMode("none")
                    .withBinds(new Bind(tempDir.toAbsolutePath().toString(), new Volume(CONTAINER_WORK_DIR)));

            String command = "javac " + className + ".java && timeout "
                    + Math.max(1, timeoutMs / 1000) + " java " + className + " < input.txt";

            // создание контейнера, обернутое docker retry wrap
            CreateContainerResponse container = withDockerRetry(() ->
                    dockerClient.createContainerCmd(arenaImage)
                            .withHostConfig(hostConfig)
                            .withWorkingDir(CONTAINER_WORK_DIR)
                            .withCmd("sh", "-c", command)
                            .exec()
            );

            containerId = container.getId();
            String finalContainerId = containerId;
            withDockerRetry(() -> {
                dockerClient.startContainerCmd(finalContainerId).exec();
                return null;
            });

            Integer exitCode = withDockerRetry(() ->
                    dockerClient.waitContainerCmd(finalContainerId)
                            .start()
                            .awaitStatusCode(timeoutMs + 1_000L, TimeUnit.MILLISECONDS)
            );

            if (exitCode == null) {
                try {
                    dockerClient.killContainerCmd(containerId).exec();
                } catch (Exception ignored) {
                    // Контейнер уже мог завершиться.
                }
                return ExecutionResult.builder()
                        .stdout("")
                        .stderr("Time limit exceeded")
                        .exitCode(124)
                        .executionTimeMs(System.currentTimeMillis() - startedAt)
                        .status("TIMEOUT")
                        .timedOut(true)
                        .build();
            }

            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();

            withDockerRetry(() -> {
                dockerClient.logContainerCmd(finalContainerId)
                        .withStdOut(true)
                        .withStdErr(true)
                        .withTimestamps(false)
                        .exec(new LogContainerResultCallback() {
                            @Override
                            public void onNext(Frame frame) {
                                String payload = new String(frame.getPayload(), StandardCharsets.UTF_8);
                                if (frame.getStreamType() == StreamType.STDERR) {
                                    stderr.append(payload);
                                } else {
                                    stdout.append(payload);
                                }
                                super.onNext(frame);
                            }
                        }).awaitCompletion();
                return null;
            });

            String status = classifyError(stderr.toString());
            return ExecutionResult.builder()
                    .stdout(stdout.toString().trim())
                    .stderr(stderr.toString().trim())
                    .exitCode(exitCode)
                    .executionTimeMs(System.currentTimeMillis() - startedAt)
                    .status(status)
                    .timedOut(false)
                    .build();
        } catch (Exception ex) {
            throw new DockerExecutionException("Execution failed in docker sandbox: " + ex.getMessage());
        } finally {
            safeRemoveContainer(containerId);
        }
    }

    @Override
    public String getLanguage() {
        return "JAVA";
    }

    private String extractClassName(String sourceCode) {
        Matcher matcher = PUBLIC_CLASS_PATTERN.matcher(sourceCode == null ? "" : sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new DockerExecutionException("Java source must contain a public class declaration");
    }

    private void safeRemoveContainer(String containerId) {
        if (containerId == null) return;

        try {
            withDockerRetry(() -> {
                dockerClient.removeContainerCmd(containerId)
                        .withForce(true)
                        .exec();
                return null;
            });
        } catch (Exception ignored) {
            // Final fallback — do not break execution flow
        }
    }

    private void cleanupTempDirectory(Path tempDir) {
        try (var paths = Files.walk(tempDir)) {
            paths.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup.
                        }
                    });
        } catch (IOException ignored) {
            // Best-effort cleanup.
        }
    }

    private String classifyError(String stderr) {
        if (stderr == null || stderr.isBlank()) {
            return "SUCCESS";
        }

        String err = stderr.toLowerCase();

        if (err.contains("error:") && !err.contains("exception")) {
            return "COMPILE_ERROR";
        }

        if (err.contains("could not find or load main class") ||
                err.contains("classnotfoundexception")) {
            return "CLASS_NOT_FOUND";
        }

        if (err.contains("outofmemoryerror") ||
                err.contains("java heap space") ||
                err.contains("gc overhead limit exceeded")) {
            return "MEMORY_LIMIT_EXCEEDED";
        }

        if (err.contains("killed") || err.contains("cannot allocate memory")) {
            return "MEMORY_LIMIT_EXCEEDED";
        }

        if (err.contains("exception in thread") ||
                err.contains("at ") ||
                err.contains("caused by")) {
            return "RUNTIME_ERROR";
        }

        return "FAILED";
    }
}
