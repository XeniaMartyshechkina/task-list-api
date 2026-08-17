# todo-jpa

`todo-jpa` is a Spring Boot 4 REST API for managing people, accounts, and tasks with PostgreSQL persistence and AI-assisted task enrichment.

The core idea is:

- a `Person` owns one or more `Account`s
- an `Account` owns one or more `Task`s
- when a task is created, OpenAI is used to generate a concise summary, a category, and a suggested priority before the task is persisted

The project is structured around JPA entities, a service layer, and thin REST controllers.

## Features

- Create a person with a hashed password and default `USER` role
- Seed an admin person at startup from environment variables
- Create accounts for the currently authenticated person
- Create tasks for an account owned by the currently authenticated person
- Enrich tasks through OpenAI before persistence
- Update task priority
- Update account status
- Retrieve the current person with nested accounts and tasks
- Query all persons or accounts by status

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Security
- OpenAI Java SDK (`openai-java`)
- JUnit 5
- Mockito

## Domain Model

### Person

- Primary key: `email`
- Fields: `firstName`, `lastName`, `address`, `role`, `passwordHash`
- Relationships: one-to-many with `Account`

### Account

- Primary key: generated `id`
- Fields: `title`, `status`
- Relationships: one-to-many with `Task`

### Task

- Primary key: generated `id`
- Fields: `summary`, `description`, `status`, `category`, `priority`

### Enums

- `PersonRoleEnum`: `ADMIN`, `USER`
- `AccountStatusEnum`: `ACTIVE`, `BLOCKED`
- `TaskStatusEnum`: `TO_DO`, `DONE`, `CANCELED`
- `TaskCategoryEnum`: `PERSONAL`, `BUSINESS`, `HEALTH`, `OTHER`
- `TaskPriorityEnum`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

## Architecture

```text
src/main/java/ch/xenia/todojpa
|- config
|  |- AdminInitializer.java
|  `- SecurityConfig.java
|- domain
|  |- Person.java
|  |- Account.java
|  |- Task.java
|  `- ...enums
|- exception
|  |- AiServiceException.java
|  `- GlobalExceptionHandler.java
|- service
|  |- ToDoPersistenceService.java
|  |- TaskPersistenceService.java
|  `- AiTaskAnalysisService.java
`- web
   |- controller
   |  |- CreationController.java
   |  `- RetrievalController.java
   `- dto
      |- request
      `- response
```

### Responsibility split

- `CreationController` exposes write endpoints.
- `RetrievalController` exposes read endpoints.
- `ToDoPersistenceService` contains the main business logic.
- `AiTaskAnalysisService` calls OpenAI and converts the response into structured task metadata.
- `TaskPersistenceService` enforces account ownership during task creation before attaching the task to an account.
- `AdminInitializer` creates the initial admin record when the application starts.

## How Task Creation Works

Task creation is the most important workflow in the project.

1. A client sends `POST /api/accounts/{accountId}/tasks` with a task description.
2. `ToDoPersistenceService.createTask(...)` forwards the request to `AiTaskAnalysisService`.
3. `AiTaskAnalysisService` prompts OpenAI to return:
   - a one-line summary
   - a category from `PERSONAL`, `BUSINESS`, `HEALTH`, `OTHER`
   - a priority from `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
4. The service maps the AI response into a new `Task` entity.
5. The task is initialized with:
   - `description` from the request
   - `summary` from AI
   - `status = TO_DO`
   - `category` from AI
   - `priority` from AI
6. `TaskPersistenceService.persistTask(...)` loads the account only if it belongs to the authenticated person.
7. The task is attached to the account and persisted through the account relationship.

This flow is explicitly validated by `ToDoServiceMockitoTest`, which verifies that:

- the AI analysis service is called with the original request
- the task persistence service is called with the mapped `Task`, `accountId`, and `email`
- the created task contains the expected description, AI summary, default status, AI-derived category, and AI-derived priority

## Project Structure and Data Flow

### Persistence model

- `Person -> Account` uses `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` with `person_email` as the join column.
- `Account -> Task` uses `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` with `account_id` as the join column.

That means child entities are saved through the parent aggregate instead of being persisted independently in the current code path.

### Error handling

`GlobalExceptionHandler` currently maps:

- `EntityNotFoundException` to HTTP `404 Not Found`
- `EntityExistsException` to HTTP `409 Conflict`

## Configuration

Main application properties are in `src/main/resources/application.properties`.

### Default server settings

- Port: `9090`
- Context path: `/app`
- Base URL: `http://localhost:9090/app`

### Default database settings

The main profile expects PostgreSQL:

- Database URL: `jdbc:postgresql://localhost:5432/todo_jpa`
- Username: `postgres`
- Password: `postgres`
- `spring.jpa.hibernate.ddl-auto=update`

The test profile also uses PostgreSQL:

- Database URL: `jdbc:postgresql://localhost:5432/todo_jpa_test`
- `spring.jpa.hibernate.ddl-auto=create-drop`

### Environment variables

The application expects the following environment variables:

| Variable | Required | Purpose |
| --- | --- | --- |
| `OPENAI_API_KEY` | Yes for task creation | Used by `OpenAIOkHttpClient.fromEnv()` |
| `OPENAI_MODEL` | No | Overrides the model; default is `gpt-5.6-luna` |
| `ADMIN_EMAIL` | Yes | Email for the seeded admin person |
| `ADMIN_PASSWORD` | Yes | Password for the seeded admin person |
| `ADMIN_FIRST_NAME` | No | Defaults to `admin` |
| `ADMIN_LAST_NAME` | No | Defaults to `admin` |
| `ADMIN_ADDRESS` | No | Defaults to `Geneva` |

### Example PowerShell environment setup

```powershell
$env:OPENAI_API_KEY="your-openai-api-key"
$env:OPENAI_MODEL="gpt-5.6-luna"
$env:ADMIN_EMAIL="admin@example.com"
$env:ADMIN_PASSWORD="Admin12345"
$env:ADMIN_FIRST_NAME="System"
$env:ADMIN_LAST_NAME="Admin"
$env:ADMIN_ADDRESS="Geneva"
```

## Local Setup

### 1. Create PostgreSQL databases

```sql
CREATE DATABASE todo_jpa;
CREATE DATABASE todo_jpa_test;
```

### 2. Use Java 21 and PostgreSQL

You need:

- JDK 21
- a running PostgreSQL instance
- Maven

### 3. Configure environment variables (Run → edit configurations → env. variables)

- `OPENAI_API_KEY`
- `ADMIN_EMAIL`
- `ADMIN_PASSWORD`

### 4. Start the application

Examples:

```powershell
mvn spring-boot:run
```

or from IntelliJ IDEA, run `TodoJpaApplication`.

### 5. Confirm the application base URL

Once started, the API is available under:

```text
http://localhost:9090/app
```

## API

All routes are prefixed with `/app/api`.

### 1. Create a person

`POST /app/api/persons`

Request body:

```json
{
  "email": "xenia@test.com",
  "firstName": "Xenia",
  "lastName": "Balashova",
  "address": "Geneva",
  "password": "Test12345"
}
```

Validation rules:

- `email` must be a valid email
- all fields are required
- `password` must be 8 to 100 characters
- `password` must contain at least one uppercase letter, one lowercase letter, and one digit

Response example:

```json
{
  "email": "xenia@test.com",
  "firstName": "Xenia",
  "lastName": "Balashova",
  "address": "Geneva",
  "role": "USER",
  "accounts": []
}
```

### 2. Create an account for the authenticated person

`POST /app/api/accounts`

Request body:

```json
{
  "title": "my professional account"
}
```

Response example:

```json
{
  "id": 1,
  "title": "my professional account",
  "status": "ACTIVE",
  "tasks": []
}
```

### 3. Create a task for an owned account

`POST /app/api/accounts/{accountId}/tasks`

Request body:

```json
{
  "description": "Implement role-based access control in the Spring Boot application using Spring Security."
}
```

Response example:

```json
{
  "id": 1,
  "summary": "Implement role-based access control",
  "description": "Implement role-based access control in the Spring Boot application using Spring Security.",
  "status": "TO_DO",
  "category": "BUSINESS",
  "priority": "HIGH"
}
```

Notes:

- `summary`, `category`, and `priority` are AI-generated
- actual AI output may vary
- the AI response must still map to the defined enums or task creation will fail

### 4. Update task priority

`PUT /app/api/tasks/{taskId}/priority`

Intended access: admin only

Request body:

```json
{
  "priority": "CRITICAL"
}
```

Response: `204 No Content`

### 5. Update account status

`PUT /app/api/accounts/{accountId}/status`

Intended access: admin only

Request body:

```json
{
  "status": "BLOCKED"
}
```

Response: `204 No Content`

### 6. Get the current person with accounts and tasks

`GET /app/api/me`

Response example:

```json
{
  "email": "xenia@test.com",
  "firstName": "Xenia",
  "lastName": "Balashova",
  "address": "Geneva",
  "role": "USER",
  "accounts": [
    {
      "id": 1,
      "title": "my professional account",
      "status": "ACTIVE",
      "tasks": [
        {
          "id": 1,
          "summary": "Implement role-based access control",
          "description": "Implement role-based access control in the Spring Boot application using Spring Security.",
          "status": "TO_DO",
          "category": "BUSINESS",
          "priority": "HIGH"
        }
      ]
    }
  ]
}
```

### 7. Get all persons

`GET /app/api/persons`

Intended access: admin only

Current return type: `List<Person>`

### 8. Get accounts by status

`GET /app/api/accounts?status=ACTIVE`

Intended access: admin only

Current return type: `List<Account>`

## Testing

Tests live under `src/test/java/ch/xenia/todojpa`.

### `TodoJpaApplicationTests`

- Basic Spring context smoke test

### `ToDoServiceTest`

- Full Spring Boot integration test
- Uses the `test` profile
- Uses a PostgreSQL test database (`todo_jpa_test`)
- Mocks `AiTaskAnalysisService` with `@MockitoBean`
- Covers:
  - person creation
  - account creation
  - task creation
  - account status update
  - person lookup
  - account lookup by status
  - listing all persons

### `ToDoServiceMockitoTest`

This is the most focused unit test for the task creation workflow.

It uses:

- `@ExtendWith(MockitoExtension.class)`
- mocked `EntityManager`
- mocked `AiTaskAnalysisService`
- mocked `TaskPersistenceService`
- `@InjectMocks` on `ToDoPersistenceService`

What it verifies:

- `createTask(...)` calls the AI analysis service
- the AI result is copied into the new `Task`
- `TaskStatusEnum.TO_DO` is assigned by default
- the mapped task is passed to `TaskPersistenceService.persistTask(...)`
- the returned task is not null and preserves the original description

If you change the task creation contract, this test should usually be updated first because it captures the orchestration behavior without requiring a running database.

## Useful Implementation Notes

- `createPerson(...)` rejects duplicate emails with `409 Conflict`.
- `createAccountForAPerson(...)` and task creation both depend on the authenticated principal name matching a stored `Person.email`.
- `findAllPersons()` returns all `Person` entities directly.
- `findAccountsWithStatus(...)` filters accounts using JPQL.

## Known Gaps and Follow-Up Ideas

- Add HTTP-level controller tests.
- Consider a fallback strategy when AI output does not match the expected enums.

