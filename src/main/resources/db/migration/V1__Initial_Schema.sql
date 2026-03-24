-- 1. Таблица Пользователи
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       age INT,
                       role VARCHAR(20) NOT NULL,
                       fav_category_id INT,
                       region VARCHAR(100),
                       registration_date DATE DEFAULT CURRENT_DATE,
                       phone_number VARCHAR(20)
);


CREATE TABLE groups (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        teacher_id INT REFERENCES users(id) ON DELETE SET NULL
);


CREATE TABLE student_groups (
                                student_id INT REFERENCES users(id) ON DELETE CASCADE,
                                group_id INT REFERENCES groups(id) ON DELETE CASCADE,
                                PRIMARY KEY (student_id, group_id)
);


CREATE TABLE tasks (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       time_limit_ms INT DEFAULT 2000,
                       memory_limit_mb INT DEFAULT 256,
                       author_id INT REFERENCES users(id) ON DELETE SET NULL
);


CREATE TABLE test_cases (
                            id SERIAL PRIMARY KEY,
                            task_id INT REFERENCES tasks(id) ON DELETE CASCADE,
                            input_data TEXT,
                            expected_output TEXT,
                            is_hidden BOOLEAN DEFAULT FALSE
);


CREATE TABLE submissions (
                             id SERIAL PRIMARY KEY,
                             user_id INT REFERENCES users(id) ON DELETE CASCADE,
                             task_id INT REFERENCES tasks(id) ON DELETE CASCADE,
                             source_code TEXT NOT NULL,
                             language VARCHAR(50) NOT NULL,
                             status VARCHAR(50) NOT NULL,
                             execution_time_ms INT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE security_logs (
                               id SERIAL PRIMARY KEY,
                               submission_id INT REFERENCES submissions(id) ON DELETE CASCADE,
                               event_type VARCHAR(100) NOT NULL,
                               timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);