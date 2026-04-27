package org.example.execution.executor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
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

@Component
@RequiredArgsConstructor
public class PythonExecutor implements CodeExecutor {

    private static final String CONTAINER_WORK_DIR = "/workspace";
    private static final String PYTHON_FILE_NAME = "main.py";

    private final DockerClient dockerClient;

    @Value("${application.execution.python-image:codeguard-arena-python:latest}")
    private String arenaImage;

    @Value("${application.execution.cpu-set:0}")
    private String cpuSet;

    @Value("${application.execution.default-memory-mb:128}")
    private int defaultMemoryMb;


    @Override
    public ExecutionResult execute(String sourceCode, String inputData, int timeoutMs, int memoryMb) {
        int memoryLimitMb = memoryMb > 0 ? memoryMb : defaultMemoryMb;
        long startedAt = System.currentTimeMillis();

        Path tempDir = null;
        String containerId = null;

        try {
            tempDir = Files.createTempDirectory("codeguard-python-run-");

            Files.writeString(
                    tempDir.resolve(PYTHON_FILE_NAME),
                    sourceCode,
                    StandardCharsets.UTF_8
            );

            Files.writeString(
                    tempDir.resolve("input.txt"),
                    inputData == null ? "" : inputData,
                    StandardCharsets.UTF_8
            );

            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withAutoRemove(true)
                    .withMemory((long) memoryLimitMb * 1024 * 1024)
                    .withCpusetCpus(cpuSet)
                    .withNetworkMode("none")
                    .withBinds(new Bind(
                            tempDir.toAbsolutePath().toString(),
                            new Volume(CONTAINER_WORK_DIR)
                    ));

            String command = "timeout " + Math.max(1, timeoutMs / 1000)
                    + " python3 " + PYTHON_FILE_NAME + " < input.txt";

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
            throw new DockerExecutionException(
                    "Execution failed in docker sandbox: " + ex.getMessage()
            );
        } finally {
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId)
                            .withForce(true)
                            .exec();
                } catch (Exception ignored) {
                }
            }

            if (tempDir != null) {
                cleanupTempDirectory(tempDir);
            }
        }
    }

    @Override
    public String getLanguage() {
        return "PYTHON";
    }

    private void cleanupTempDirectory(Path tempDir) {
        try (var paths = Files.walk(tempDir)) {
            paths.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private String classifyError(String stderr) {

        if (stderr == null || stderr.isBlank()) {
            return "SUCCESS";
        }

        String err = stderr.toLowerCase();

        if (err.contains("syntaxerror") || err.contains("indentationerror")) {
            return "SYNTAX_ERROR";
        }

        if (err.contains("modulenotfounderror") || err.contains("importerror")) {
            return "IMPORT_ERROR";
        }

        if (err.contains("memoryerror") ||
                err.contains("killed") ||                // container killed (OOM)
                err.contains("cannot allocate memory")) {
            return "MEMORY_LIMIT_EXCEEDED";
        }

        if (err.contains("traceback")) {
            return "RUNTIME_ERROR";
        }

        return "SYSTEM_ERROR";
    }
}

