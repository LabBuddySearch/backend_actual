package org.example.execution.factory;

import org.example.exception.DockerExecutionException;
import org.example.exception.NotFoundException;
import org.example.execution.executor.CodeExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExecutionStrategyFactory {

    private final Map<String, CodeExecutor> executors;

    public ExecutionStrategyFactory(List<CodeExecutor> executorList) {
        this.executors = executorList.stream()
                .collect(Collectors.toMap(
                        executor -> executor.getLanguage().toUpperCase(),
                        Function.identity()
                ));
    }

    public CodeExecutor getStrategy(String language) {
        // Необходима ли данная проверка, если запрос, в котором не указан язык, отсеится на этапе валидации?
        if (language == null || language.isBlank()) {
            throw new DockerExecutionException("Programming language must be provided");
        }

        CodeExecutor executor = executors.get(language.toUpperCase());
        if (executor == null) {
            throw new NotFoundException("Language " + language + " is not supported yet");
        }
        return executor;
    }
}

