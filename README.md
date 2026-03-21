# Pet Management REST API

A production-ready Spring Boot REST application for managing pets, built with a layered architecture and a port/adapter persistence abstraction designed for future database migration.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [How to Build and Run](#how-to-build-and-run)
6. [API Documentation](#api-documentation)
7. [Testing with Postman and curl](#testing-with-postman-and-curl)
8. [Security Configuration](#security-configuration)
9. [H2 Console](#h2-console)
10. [Switching from H2 to PostgreSQL](#switching-from-h2-to-postgresql)
11. [Running Tests and Coverage Reports](#running-tests-and-coverage-reports)

---

## Project Overview

This application exposes a REST API to perform CRUD operations on a `Pet` entity. It is built as a senior-grade, production-ready project following clean code principles, layered architecture, and interface-driven persistence abstraction.

---

## Architecture

The application follows a three-layer architecture:

```
Controller → Service → Repository (Port/Adapter)
```

### Layered Architecture

| Layer | Responsibility |
|---|---|
| **Controller** | Accepts HTTP requests, delegates to service, returns HTTP responses |
| **Service** | Contains business logic; operates on domain entities via the persistence port |
| **Repository** | Persistence abstraction (port) and its JPA implementation (adapter) |

### Port/Adapter Pattern for Persistence

The repository layer uses a port/adapter pattern to decouple business logic from the persistence technology:

- `PetPersistencePort` — interface defining the contract used by the service layer
- `JpaPetPersistenceAdapter` — JPA implementation of the port, backed by Spring Data JPA
- `JpaPetRepository` — internal Spring Data JPA repository used by the adapter

To switch from a relational database (H2/PostgreSQL) to a non-relational one (e.g., MongoDB), you only need to:
1. Create a new adapter implementing `PetPersistencePort` (e.g., `MongoPetPersistenceAdapter`)
2. Register it as the active Spring bean
3. No changes to the service layer are required

---

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Programming language |
| Spring Boot | 3.2.x | Application framework |
| Spring Web | 3.2.x | REST API |
| Spring Data JPA | 3.2.x | ORM and data access |
| Spring Security | 3.2.x | Authentication and authorization |
| Spring Validation | 3.2.x | Bean validation |
| H2 | Runtime | In-memory database (development) |
| JUnit 5 | 5.x | Testing framework |
| Mockito | 5.x | Mocking framework |
| AssertJ | 3.x | Fluent assertions |
| JaCoCo | 0.8.11 | Code coverage |
| Maven | 3.9+ | Build tool |

---

## Project Structure

```
src/
  main/
    java/com/petmanager/
      config/
        SecurityConfig.java          # Spring Security configuration
      controller/
        PetController.java           # REST endpoints
      dto/
        PetRequest.java              # Incoming request DTO with validation
        PetResponse.java             # Outgoing response DTO
        ErrorResponse.java           # Error payload
        ValidationErrorResponse.java # Validation error payload
      entity/
        Pet.java                     # JPA entity
      exception/
        PetNotFoundException.java    # Domain exception
        GlobalExceptionHandler.java  # @RestControllerAdvice
      mapper/
        PetMapper.java               # Entity <-> DTO conversion
      repository/
        PetPersistencePort.java      # Port interface
        JpaPetRepository.java        # Spring Data JPA repository
        JpaPetPersistenceAdapter.java# JPA adapter implementing the port
      service/
        PetService.java              # Service interface
        PetServiceImpl.java          # Service implementation
      PetManagerApplication.java
    resources/
      application.yml                # Base configuration (activates dev profile)
      application-dev.yml            # H2 dev profile
      application-prod.yml           # PostgreSQL production profile template
  test/
    java/com/petmanager/
      controller/
        PetControllerTest.java       # Integration tests (MockMvc)
      exception/
        PetNotFoundExceptionTest.java
      mapper/
        PetMapperTest.java
      repository/
        JpaPetPersistenceAdapterTest.java
      service/
        PetServiceImplTest.java      # Unit tests (Mockito)
```

---

## How to Build and Run

### Prerequisites

- Java 17+
- Maven 3.9+

### Build

```bash
mvn clean package
```

### Run (development profile with H2)

```bash
mvn spring-boot:run
```

Or run the packaged jar:

```bash
java -jar target/pet-manager-1.0.0.jar
```

The application will start on `http://localhost:8080` with the `dev` profile active.

### Run with production profile

```bash
java -jar target/pet-manager-1.0.0.jar --spring.profiles.active=prod
```

---

## API Documentation

### Base URL

```
http://localhost:8080/api/v1/pets
```

### Endpoints

| Method | Path | Description | Auth required |
|---|---|---|---|
| POST | `/api/v1/pets` | Create a pet | ADMIN |
| GET | `/api/v1/pets` | Retrieve all pets | USER or ADMIN |
| GET | `/api/v1/pets/{id}` | Retrieve a pet by ID | USER or ADMIN |
| PUT | `/api/v1/pets/{id}` | Update a pet | ADMIN |
| DELETE | `/api/v1/pets/{id}` | Delete a pet | ADMIN |

### Pet Entity Fields

| Field | Type | Required | Constraints |
|---|---|---|---|
| `id` | Long | auto-generated | — |
| `name` | String | yes | not blank |
| `species` | String | yes | not blank |
| `age` | Integer | no | >= 0 |
| `ownerName` | String | no | — |

### HTTP Status Codes

| Status | Meaning |
|---|---|
| 201 Created | Pet successfully created |
| 200 OK | Request succeeded |
| 204 No Content | Pet successfully deleted |
| 400 Bad Request | Validation failed |
| 401 Unauthorized | Missing or invalid credentials |
| 403 Forbidden | Authenticated but not authorized |
| 404 Not Found | Pet not found |
| 500 Internal Server Error | Unexpected error |

---

## Testing with Postman and curl

### Credentials

| Username | Password | Roles |
|---|---|---|
| `user` | `user123` | USER |
| `admin` | `admin123` | ADMIN |

### Create a pet

```bash
curl -X POST http://localhost:8080/api/v1/pets \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy",
    "species": "Dog",
    "age": 3,
    "ownerName": "John Doe"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "Buddy",
  "species": "Dog",
  "age": 3,
  "ownerName": "John Doe"
}
```

### Retrieve all pets

```bash
curl -X GET http://localhost:8080/api/v1/pets \
  -u user:user123
```

### Retrieve a pet by ID

```bash
curl -X GET http://localhost:8080/api/v1/pets/1 \
  -u user:user123
```

### Update a pet

```bash
curl -X PUT http://localhost:8080/api/v1/pets/1 \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy Updated",
    "species": "Dog",
    "age": 4,
    "ownerName": "John Doe"
  }'
```

### Delete a pet

```bash
curl -X DELETE http://localhost:8080/api/v1/pets/1 \
  -u admin:admin123
```

**Response: 204 No Content**

### Validation error example

```bash
curl -X POST http://localhost:8080/api/v1/pets \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "species": "Dog",
    "age": -1
  }'
```

**Response (400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {"field": "name", "message": "Name is required"},
    {"field": "age", "message": "Age must be greater than or equal to 0"}
  ],
  "timestamp": "2024-01-01T12:00:00"
}
```

### Not found error example

```bash
curl -X GET http://localhost:8080/api/v1/pets/999 \
  -u user:user123
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "message": "Pet not found with id: 999",
  "timestamp": "2024-01-01T12:00:00"
}
```

---

## Security Configuration

Spring Security is configured in `SecurityConfig.java` with HTTP Basic authentication.

### Access Rules

| Endpoint | Required Role |
|---|---|
| `GET /api/v1/pets/**` | USER or ADMIN |
| `POST /api/v1/pets/**` | ADMIN |
| `PUT /api/v1/pets/**` | ADMIN |
| `DELETE /api/v1/pets/**` | ADMIN |
| `/h2-console/**` | Public (development only) |

### Default Users (in-memory)

| Username | Password | Role |
|---|---|---|
| `user` | `user123` | USER |
| `admin` | `admin123` | ADMIN |

### Extending Security

For production use, replace `InMemoryUserDetailsManager` with a database-backed `UserDetailsService`:

1. Create a `User` entity and repository
2. Implement `UserDetailsService` to load users from the database
3. Register it as a Spring bean; Spring Security will use it automatically

CSRF protection is disabled for simplicity with REST clients. For browser-based clients, enable it in `SecurityConfig`.

---

## H2 Console

When running with the `dev` profile, the H2 in-memory database console is available at:

```
http://localhost:8080/h2-console
```

**Connection settings:**

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:petdb` |
| Username | `sa` |
| Password | *(leave empty)* |

---

## Switching from H2 to PostgreSQL

### 1. Add PostgreSQL dependency to `pom.xml`

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

You may also remove or scope the H2 dependency to `test`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. Update `application-prod.yml`

The file `src/main/resources/application-prod.yml` is already prepared as a template:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/petdb
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
```

Set the environment variables `DB_USERNAME` and `DB_PASSWORD` before running.

### 3. Create the database schema

With `ddl-auto: validate`, Hibernate will validate the schema but not create it. Use a migration tool such as Flyway or Liquibase to manage the schema:

```sql
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(255) NOT NULL,
    age INTEGER,
    owner_name VARCHAR(255)
);
```

### 4. Run with the prod profile

```bash
export DB_USERNAME=youruser
export DB_PASSWORD=yourpassword
java -jar target/pet-manager-1.0.0.jar --spring.profiles.active=prod
```

---

## Running Tests and Coverage Reports

### Run all tests

```bash
mvn test
```

### Run tests and generate JaCoCo coverage report

```bash
mvn verify
```

The HTML coverage report is generated at:

```
target/site/jacoco/index.html
```

Open it in a browser to view the detailed coverage breakdown.

### Coverage threshold

The build is configured to fail if line coverage drops below **90%** (excluding DTOs and the application entry point). This is enforced by the JaCoCo Maven plugin during the `verify` phase.

### Test structure

| Test class | Type | What it covers |
|---|---|---|
| `PetServiceImplTest` | Unit (Mockito) | Service layer business logic |
| `PetControllerTest` | Integration (MockMvc) | Controller endpoints, security, validation |
| `JpaPetPersistenceAdapterTest` | Integration (@DataJpaTest) | Repository adapter with H2 |
| `PetMapperTest` | Unit | DTO/Entity mapping |
| `PetNotFoundExceptionTest` | Unit | Exception message |

