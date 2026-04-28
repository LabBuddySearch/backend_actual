package org.example.execution.factory;

import org.example.exception.NotFoundException;
import org.example.execution.executor.CodeExecutor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExecutorFactory {

    private final Map<String, CodeExecutor> executors;

    public ExecutorFactory(List<CodeExecutor> executorList) {
        this.executors = executorList.stream()
                .collect(Collectors.toMap(
                        executor -> executor.getLanguage().toUpperCase(),
                        Function.identity()
                ));
    }

    public CodeExecutor getExecutor(String language) {
        CodeExecutor executor = executors.get(language.toUpperCase());
        if (executor == null) {
            throw new NotFoundException("Язык " + language + " пока не поддерживается.");
        }
        return executor;
    }
}
