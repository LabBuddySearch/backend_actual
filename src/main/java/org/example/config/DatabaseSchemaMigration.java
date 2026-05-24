package org.example.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Дополняет схему, если ddl-auto не добавил новые колонки (часто на уже существующих таблицах).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void applyPatches() {
        patch(
                "assign_to_all_teacher_groups",
                "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS assign_to_all_teacher_groups BOOLEAN NOT NULL DEFAULT false"
        );
        patch(
                "category",
                "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS category VARCHAR(32) NOT NULL DEFAULT 'ALGORITHMS'"
        );
        patch(
                "deadline_at",
                "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deadline_at TIMESTAMP"
        );
        patch(
                "max_attempts",
                "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS max_attempts INTEGER NOT NULL DEFAULT 3"
        );
    }

    private void patch(String name, String sql) {
        try {
            jdbcTemplate.execute(sql);
            log.info("Schema patch applied: tasks.{}", name);
        } catch (Exception ex) {
            log.error("Failed to apply schema patch for tasks.{}: {}", name, ex.getMessage());
        }
    }
}
