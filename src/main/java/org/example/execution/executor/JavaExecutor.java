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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class JavaExecutor implements CodeExecutor {

    private static final Pattern PUBLIC_CLASS_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z_$][A-Za-z\\d_$]*)");
    private static final String CONTAINER_WORK_DIR = "/workspace";

    private final DockerClient dockerClient;

    @Value("${application.execution.arena-image:codeguard-arena-java:latest}")
    private String arenaImage;

    @Value("${application.execution.cpu-set:0}")
    private String cpuSet;

    @Value("${application.execution.default-memory-mb:128}")
    private int defaultMemoryMb;

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

            CreateContainerResponse container = dockerClient.createContainerCmd(arenaImage)
                    .withHostConfig(hostConfig)
                    .withWorkingDir(CONTAINER_WORK_DIR)
                    .withCmd("sh", "-c", command)
                    .exec();

            containerId = container.getId();
            dockerClient.startContainerCmd(containerId).exec();

            Integer exitCode = dockerClient.waitContainerCmd(containerId)
                    .start()
                    .awaitStatusCode(timeoutMs + 1_000L, TimeUnit.MILLISECONDS);

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

            dockerClient.logContainerCmd(containerId)
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

            String status = exitCode == 0 ? "SUCCESS" : "FAILED";
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
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception ignored) {
                    // withAutoRemove уже чистит контейнер, это fallback.
                }
            }
            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
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
}
