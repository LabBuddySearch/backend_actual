# CodeGuard

Backend judge-платформа (Spring Boot 3): задачи, тест-кейсы, отправка решений на **JAVA** и **PYTHON** через Docker-арену.

## Быстрый старт (локально)

1. Поднять PostgreSQL и Redis, например: `docker compose up -d db redis`
2. Собрать образы арен (один раз):  
   `docker compose --profile build-only build execution_arena_image execution_arena_python`
3. Запуск API: `./gradlew bootRun`  
4. OpenAPI UI: `http://localhost:8080/swagger-ui.html`

Переменные окружения (прод и стенды) см. в `src/main/resources/application.yml` — префиксы `DATABASE_*`, `JWT_SECRET`, `APP_CORS_ORIGINS`, `APP_SUBMIT_RATE_PER_MINUTE`, `ARENA_JAVA_IMAGE`, `ARENA_PYTHON_IMAGE`.

Прод-профиль (тише логи, без SQL в консоль): **`SPRING_PROFILES_ACTIVE=prod`** → подхватывается `application-prod.yml`.

---

## Преподаватель и студенты (как устроено сейчас)

**Приглашений, «курсов» и REST API для добавления студентов к преподавателю в этом репозитории нет.**

- **Каталог задач:** `GET /api/tasks` возвращает **все** задачи из базы для любого аутентифицированного пользователя (и студент, и преподаватель).
- **Автор задачи:** в каждом элементе списка поле **`author`** — это **email автора** (не ФИО). Преподавательский сценарий «только мои задачи» на MVP делается на **фронтенде**: отфильтровать список, где `author` совпадает с email пользователя с ролью `TEACHER` из JWT/`AuthResponse`.
- **Студент** видит те же задачи в каталоге; ограничения «только задачи моего преподавателя» бэкендом **не** навешиваются — любой студент может открыть любую задачу по id и отправить решение (если знает id).
- В доменной модели есть сущности **`Group`** и **`StudentGroup`** (группа у преподавателя, связь студент–группа), но **нет контроллеров и бизнес-логики** под них; это задел на следующий этап (привязка классов, выдача задач по группе и т.д.).

Если продукту нужен именно сценарий «преподаватель закрепляет студентов», его нужно спроектировать отдельно (эндпоинты + фильтрация списка задач и сабмитов на бэкенде).

---

## Чеклист перед merge в `main` / выкладкой

1. **Переменные:** заданы `JWT_SECRET`, `DATABASE_*`, `APP_CORS_ORIGINS` (реальные origin фронта), при нагрузочных тестах — `APP_SUBMIT_RATE_PER_MINUTE`.
2. **Профиль:** для стенда/прода `SPRING_PROFILES_ACTIVE=prod`.
3. **Docker:** образы арен собраны и доступны хосту с API (`ARENA_JAVA_IMAGE` / `ARENA_PYTHON_IMAGE`).
4. **Smoke:** `login` → `GET /api/tasks` → `GET /api/tasks/{id}` → `POST /api/submissions/{taskId}` → ожидаемый вердикт.
5. **Swagger в проде:** по политике безопасности закрыть на reverse proxy (пути `/swagger-ui*`, `/v3/api-docs*`).

Локально: `./gradlew test` должен проходить.

---

## Документация для фронтенда: связка с API

### Базовый URL

По умолчанию API: `http://localhost:8080`. Все пути ниже относительно этого origin.

### CORS

Сервер отдаёт CORS с **фиксированным списком origin** (нельзя `*` вместе с credentials). Список задаётся переменной **`APP_CORS_ORIGINS`** — строка с origin через запятую, без пробелов вокруг запятых или с пробелами (они обрезаются):

Пример: `APP_CORS_ORIGINS=https://app.example.com,https://admin.example.com`

Локально по умолчанию разрешены `http://localhost:3000` и `http://localhost:5173`.

Запросы с фронта: заголовок **`Authorization: Bearer <accessToken>`** после логина/регистрации.

---

### 1. Регистрация и вход

| Метод | Путь | Тело | Ответ |
|--------|------|------|--------|
| `POST` | `/api/auth/register` | `RegisterRequest` (email, пароль, ФИО, роль и т.д. — см. Swagger) | `AuthResponse` |
| `POST` | `/api/auth/login` | `LoginRequest` (email, password) | `AuthResponse` |

**`AuthResponse`**: `accessToken`, `tokenType` (обычно `"Bearer"`), `expiresInMs`, `user` (профиль и роль).

Ошибки — JSON **`ErrorResponse`**: `errorCode`, `message`, `timestamp`. Типовые коды: `VALIDATION_ERROR` (400), `UNAUTHORIZED` / `DOES_NOT_AUTHENTICATE` (401), `ACCESS_DENIED` (403), `NOT_FOUND` (404), `CONFLICT` (409), `RATE_LIMIT_EXCEEDED` (429), `INTERNAL_SERVER_ERROR` (500).

---

### 2. Задачи (студент / преподаватель)

- Список: `GET /api/tasks` — требуется JWT (любая аутентифицированная роль).
- Студент, карточка задачи (в тест-кейсах **скрытые** не отдаются):  
  `GET /api/tasks/{id}` — роль **STUDENT**.
- Преподаватель, полная карточка со всеми тестами:  
  `GET /api/tasks/teacher/{id}` — роль **TEACHER**.

Создание/редактирование/удаление — только **TEACHER**, см. Swagger (`/api/tasks/teacher/...`).

**Тест-кейсы отдельными путями (TEACHER, JWT):**

| Метод | Путь | Назначение |
|--------|------|------------|
| `POST` | `/api/test-cases/{taskId}` | Добавить тест к задаче |
| `PATCH` | `/api/test-cases/{id}` | Обновить тест |
| `DELETE` | `/api/test-cases/{id}` | Удалить тест |

Тело — `TestCaseRequest` (см. Swagger). Создание полного набора тестов при создании задачи также возможно через поле `testCases` в `NewTaskRequest` / `EditTaskRequest`.

---

### 3. Отправка решения (главный контракт для UI)

**MVP: исполнение синхронное.** Ответ `201 Created` приходит **после** прогона по тестам (время ответа может быть заметным).

| Метод | Путь | Роль | Тело |
|--------|------|------|------|
| `POST` | `/api/submissions/{taskId}` | **STUDENT** | `SubmitTaskRequest` |

**`SubmitTaskRequest`**

- `language` (обязательно): строка в верхнем регистре — **`JAVA`** или **`PYTHON`**.
- `sourceCode` (опционально): если пусто или только пробелы — решение **не** запускается в песочнице, в БД сохраняется вердикт **`EMPTY_SOURCE`**.

**`SubmissionResponse`** (успех `201`):

- `id` — идентификатор посылки (дальше по нему можно запросить детали).
- `status` — вердикт (enum строкой в JSON), см. таблицу ниже.
- `executionTimeMs` — время последнего прогона (мс).
- `language` — язык посылки.
- `stdout` / `stderr` — вывод последнего запуска (для студента может содержать диагностику; в проде можно сузить политику показа).

**Получение посылки**

- Студент (только своя): `GET /api/submissions/student/{id}`
- Преподаватель (любая): `GET /api/submissions/teacher/{id}`

**Лимит частоты:** на пользователя действует **`APP_SUBMIT_RATE_PER_MINUTE`** (по умолчанию 30 отправок в минуту). При превышении — **`429`** и `errorCode: RATE_LIMIT_EXCEEDED`.

---

### 4. Вердикты (`status`) — финальная семантика для UI

| Значение | Смысл для пользователя |
|----------|-------------------------|
| `EMPTY_SOURCE` | Пустой код, проверка не запускалась |
| `PENDING` | Зарезервировано; в текущем MVP после ответа обычно уже финальный статус |
| `ACCEPTED` | Все тесты пройдены |
| `WRONG_ANSWER` | Код выполнился, но вывод не совпал с ожидаемым |
| `COMPILATION_ERROR` | Ошибка компиляции / синтаксиса |
| `RUNTIME_ERROR` | Падение при выполнении, OOM в контейнере и т.п. |
| `TIME_LIMIT_EXCEEDED` | Превышен лимит времени |
| `INTERNAL_ERROR` | Нет тестов у задачи или внутренняя ошибка оркестратора |

Отображение: можно маппить на цвета/иконки (зелёный только `ACCEPTED`, красные оттенки для ошибок, жёлтый для `EMPTY_SOURCE`).

---

### 5. Модель исполнения (зафиксировано для MVP)

- **Сейчас:** синхронно в рамках одного HTTP-запроса: Docker-контейнер, последовательный прогон по тест-кейсам задачи.
- **Следующий этап:** очередь (submit возвращает быстрый `202` + `id`, отдельный poll/stream по статусу) — контракт путей может быть расширен, текущие поля ответа сохранят смысл.

---

### 6. DevOps / smoke (кратко)

- Образ Java-арены: `docker compose --profile build-only build execution_arena_image` → тег `codeguard-arena-java:latest` (или свой registry через `ARENA_JAVA_IMAGE`).
- Аналогично Python: сервис `execution_arena_python` → `codeguard-arena-python:latest`.
- Нагрузочный тест 20–50 параллельных submit: учитывать Docker, CPU set (`DOCKER_CPU_SET`) и rate limit; для стенда временно поднять `APP_SUBMIT_RATE_PER_MINUTE`.

---

### 7. Swagger

Интерактивная схема и примеры тел запросов: **`/swagger-ui.html`**, JSON схемы: **`/v3/api-docs`**.
