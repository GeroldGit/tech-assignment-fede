# Pet Management REST API

A production-ready Spring Boot REST application for managing pets. Built with a strict layered architecture and a port/adapter persistence abstraction that isolates the business logic from the persistence technology, making a future switch from a relational database to MongoDB (or any other store) a matter of activating a Spring profile.

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
10. [Switching Databases](#switching-databases)
    - [From H2 to PostgreSQL](#from-h2-to-postgresql)
    - [From JPA to MongoDB](#from-jpa-to-mongodb)
11. [Deployment in Various Environments](#deployment-in-various-environments)
12. [Running Tests and Coverage Reports](#running-tests-and-coverage-reports)

---

## Project Overview

This application exposes a REST API to perform CRUD operations on a `Pet` entity. It follows clean code principles, a layered architecture, and an interface-driven persistence abstraction: the service layer depends exclusively on a `PetPersistencePort` interface, never on a concrete database implementation. Concrete adapters (JPA, MongoDB) implement that interface and are activated via Spring profiles.

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│  HTTP Client (Postman, curl, browser)               │
└────────────────────┬────────────────────────────────┘
                     │  HTTP
┌────────────────────▼────────────────────────────────┐
│  PetController   (@RestController)                  │
│  /api/v1/pets                                       │
│  Validates input (Bean Validation)                  │
│  Maps DTOs ↔ domain via PetMapper                   │
└────────────────────┬────────────────────────────────┘
                     │  PetService interface
┌────────────────────▼────────────────────────────────┐
│  PetServiceImpl   (@Service)                        │
│  Business logic (create, find, update, delete)      │
│  Owns transaction boundaries (@Transactional)       │
│  Depends only on PetPersistencePort (port)          │
└────────────────────┬────────────────────────────────┘
                     │  PetPersistencePort interface
        ┌────────────┴───────────────┐
        │                            │
┌───────▼──────────┐       ┌─────────▼────────────┐
│ JpaPetPersistence│       │ MongoPetPersistence   │
│ Adapter          │       │ Adapter               │
│ @Profile("!mongo")       │ @Profile("mongo")     │
│ Spring Data JPA  │       │ Spring Data MongoDB   │
└───────┬──────────┘       └─────────┬─────────────┘
        │                            │
┌───────▼──────────┐       ┌─────────▼─────────────┐
│ JpaPetRepository │       │ MongoPetRepository    │
│ (JpaRepository)  │       │ (MongoRepository)     │
│ H2 / PostgreSQL  │       │ MongoDB               │
└──────────────────┘       └───────────────────────┘
```

### Port/Adapter Pattern for Persistence

The repository layer is structured around the hexagonal architecture (port/adapter) pattern:

| Component | Role |
|---|---|
| `PetPersistencePort` | Port — interface defining the contract consumed by the service layer |
| `JpaPetPersistenceAdapter` | Adapter — JPA implementation, active on all profiles except `mongo` |
| `MongoPetPersistenceAdapter` | Adapter — MongoDB implementation, active on the `mongo` profile |
| `JpaPetRepository` | Internal Spring Data JPA repository used by the JPA adapter |
| `MongoPetRepository` | Internal Spring Data MongoDB repository used by the Mongo adapter |
| `PetDocument` | MongoDB document class, used exclusively inside the Mongo adapter |

The service layer (`PetServiceImpl`) is completely unaware of which adapter is active. It always calls `PetPersistencePort` methods. The Spring container injects the correct adapter at startup depending on the active profile.

### Request/Response Flow

```
HTTP POST /api/v1/pets
  → PetController receives PetRequest, validates it (@Valid)
  → PetController calls PetService.create(PetRequest)
  → PetServiceImpl calls PetMapper.toEntity(PetRequest) → Pet
  → PetServiceImpl calls PetPersistencePort.save(Pet) → Pet (saved)
  → PetServiceImpl calls PetMapper.toResponse(Pet) → PetResponse
  → PetController returns 201 Created with PetResponse body
```

---

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Programming language |
| Spring Boot | 3.2.x | Application framework |
| Spring Web | 3.2.x | REST API |
| Spring Data JPA | 3.2.x | ORM and relational data access |
| Spring Data MongoDB | 3.2.x | MongoDB data access (optional, activated via `mongo` profile) |
| Spring Security | 3.2.x | Authentication and authorization |
| Spring Validation | 3.2.x | Bean validation |
| H2 | Runtime | In-memory database (dev profile) |
| PostgreSQL | — | Production relational database (prod profile) |
| MongoDB | — | NoSQL alternative (mongo profile) |
| JUnit 5 | 5.x | Testing framework |
| Mockito | 5.x | Mocking framework |
| AssertJ | 3.x | Fluent assertions |
| JaCoCo | 0.8.11 | Code coverage reporting |
| Maven | 3.9+ | Build tool |

---

## Project Structure

```
src/
  main/
    java/com/petmanager/
      config/
        SecurityConfig.java                # Spring Security filter chain, users
      controller/
        PetController.java                 # REST endpoints for /api/v1/pets
      document/
        PetDocument.java                   # MongoDB @Document class (Mongo adapter only)
      dto/
        PetRequest.java                    # Incoming request DTO with Bean Validation
        PetResponse.java                   # Outgoing response DTO
        ErrorResponse.java                 # Generic error payload
        ValidationErrorResponse.java       # Validation error payload
      entity/
        Pet.java                           # JPA @Entity / domain object
      exception/
        PetNotFoundException.java          # Thrown when a pet is not found
        GlobalExceptionHandler.java        # @RestControllerAdvice (404 / 400 / 500)
      mapper/
        PetMapper.java                     # Converts between Pet entity and DTOs
      repository/
        PetPersistencePort.java            # Port interface (contract for all adapters)
        JpaPetRepository.java              # Spring Data JPA repository (used by JPA adapter)
        JpaPetPersistenceAdapter.java      # JPA adapter, active when profile != "mongo"
        MongoPetRepository.java            # Spring Data MongoDB repository (used by Mongo adapter)
        MongoPetPersistenceAdapter.java    # MongoDB adapter, active on "mongo" profile
      service/
        PetService.java                    # Service interface
        PetServiceImpl.java                # Service implementation (uses PetPersistencePort)
      PetManagerApplication.java
    resources/
      application.yml                      # Base config (activates dev profile)
      application-dev.yml                  # H2 in-memory, H2 console, debug logging
      application-prod.yml                 # PostgreSQL, no console, production logging
      application-mongo.yml                # MongoDB, disables JPA auto-configuration
  test/
    java/com/petmanager/
      controller/
        PetControllerTest.java             # Integration tests (@WebMvcTest + MockMvc)
      exception/
        PetNotFoundExceptionTest.java      # Unit test for exception message
      mapper/
        PetMapperTest.java                 # Unit tests for DTO/entity mapping
      repository/
        JpaPetPersistenceAdapterTest.java  # Integration tests (@DataJpaTest with H2)
        MongoPetPersistenceAdapterTest.java# Unit tests for MongoDB adapter (Mockito)
      service/
        PetServiceImplTest.java            # Unit tests for service layer (Mockito)
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

### Run — development profile (H2 in-memory)

```bash
mvn spring-boot:run
```

Or using the packaged jar:

```bash
java -jar target/pet-manager-1.0.0.jar
```

The application starts on `http://localhost:8080` with the `dev` profile active (H2 in-memory database).

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

### Default Credentials

| Username | Password | Role |
|---|---|---|
| `user` | `user123` | USER (read-only) |
| `admin` | `admin123` | ADMIN (full access) |

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
  -d '{"name": "", "species": "Dog", "age": -1}'
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

### Not found example

```bash
curl -X GET http://localhost:8080/api/v1/pets/999 -u user:user123
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

Spring Security is configured in `SecurityConfig.java` with HTTP Basic authentication and stateless sessions (no server-side session state, appropriate for REST APIs).

### Access Rules

| Endpoint | Required Role |
|---|---|
| `GET /api/v1/pets/**` | USER or ADMIN |
| `POST /api/v1/pets/**` | ADMIN |
| `PUT /api/v1/pets/**` | ADMIN |
| `DELETE /api/v1/pets/**` | ADMIN |
| `/h2-console/**` | Public (dev profile only) |

### Extending Security for Production

The in-memory users are for development only. Replace `InMemoryUserDetailsManager` with a database-backed implementation:

```java
@Bean
public UserDetailsService userDetailsService(final UserRepository userRepository,
                                              final PasswordEncoder encoder) {
    return username -> userRepository.findByUsername(username)
            .map(user -> User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build())
            .orElseThrow(() -> new UsernameNotFoundException(username));
}
```

For production workloads, consider replacing HTTP Basic with JWT tokens by adding a `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter` in the security filter chain.

---

## H2 Console

When running with the `dev` profile, the H2 web console is available at:

```
http://localhost:8080/h2-console
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:petdb` |
| Username | `sa` |
| Password | *(leave empty)* |

---

## Switching Databases

### From H2 to PostgreSQL

This is a configuration-only change; no code changes are needed.

#### 1. Add the PostgreSQL driver to `pom.xml`

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Optionally, restrict H2 to the test scope:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

#### 2. Configure `application-prod.yml` (already provided as a template)

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
```

#### 3. Create the database schema

```sql
CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(255) NOT NULL,
    age INTEGER,
    owner_name VARCHAR(255)
);
```

#### 4. Run with the prod profile

```bash
export DB_USERNAME=youruser
export DB_PASSWORD=yourpassword
java -jar target/pet-manager-1.0.0.jar --spring.profiles.active=prod
```

---

### From JPA to MongoDB

The codebase already includes the complete MongoDB adapter. The swap is done by activating the `mongo` Spring profile — no service-layer or controller changes are required.

#### How the swap works

The service depends exclusively on the `PetPersistencePort` interface:

```
PetServiceImpl → PetPersistencePort (interface)
                      ↑
       ┌──────────────┴──────────────────┐
       │                                 │
JpaPetPersistenceAdapter         MongoPetPersistenceAdapter
 @Profile("!mongo")                @Profile("mongo")
 (active by default)               (active when mongo profile is set)
```

When you start with `--spring.profiles.active=mongo`, Spring activates `MongoPetPersistenceAdapter` and skips `JpaPetPersistenceAdapter`. The `PetServiceImpl` receives the MongoDB adapter injected through the same `PetPersistencePort` interface and operates without any modification.

#### Step-by-step MongoDB migration

**Step 1: Ensure the MongoDB dependency is active in `pom.xml`**

The dependency is already declared as optional. Remove the `<optional>true</optional>` tag to make it a regular compile-time dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

If you no longer need JPA, you can also remove `spring-boot-starter-data-jpa` and `h2`.

**Step 2: Configure `application-mongo.yml` (already provided)**

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/petdb}
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
```

The `autoconfigure.exclude` entries prevent Spring Boot from trying to configure JPA/JDBC when MongoDB is in use.

**Step 3: Start the application with the `mongo` profile**

```bash
export MONGODB_URI=mongodb://localhost:27017/petdb
java -jar target/pet-manager-1.0.0.jar --spring.profiles.active=mongo
```

For Docker:

```bash
docker run -e MONGODB_URI=mongodb://mongo:27017/petdb \
           -e SPRING_PROFILES_ACTIVE=mongo \
           -p 8080:8080 pet-manager:latest
```

**Step 4: Verify via the API**

```bash
curl -X POST http://localhost:8080/api/v1/pets \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"name": "Buddy", "species": "Dog", "age": 3}'
```

The response is identical regardless of the active persistence adapter. The service and controller are completely unchanged.

#### Classes involved in the MongoDB adapter

| Class | Package | Purpose |
|---|---|---|
| `PetDocument` | `com.petmanager.document` | MongoDB `@Document` representation of a Pet |
| `MongoPetRepository` | `com.petmanager.repository` | Spring Data MongoDB repository interface |
| `MongoPetPersistenceAdapter` | `com.petmanager.repository` | Implements `PetPersistencePort`, converts between `Pet` and `PetDocument` |

The `PetDocument` class mirrors the `Pet` domain object but uses `String` for the id field (MongoDB ObjectId). The adapter converts transparently between `Long` domain IDs and MongoDB's `String` ObjectIds so the service layer is never exposed to the difference.

---

## Deployment in Various Environments

### Environment Summary

| Profile | Database | Use case | How to activate |
|---|---|---|---|
| `dev` | H2 in-memory | Local development | Default (no flag needed) |
| `prod` | PostgreSQL | Production on-premise / VMs | `--spring.profiles.active=prod` |
| `mongo` | MongoDB | Production with NoSQL | `--spring.profiles.active=mongo` |

---

### Local Development (H2)

```bash
mvn spring-boot:run
# Application at http://localhost:8080
# H2 console at http://localhost:8080/h2-console
```

---

### Running with PostgreSQL (prod profile)

```bash
export DB_USERNAME=petuser
export DB_PASSWORD=secret
java -jar target/pet-manager-1.0.0.jar --spring.profiles.active=prod
```

---

### Running with MongoDB (mongo profile)

```bash
export MONGODB_URI=mongodb://localhost:27017/petdb
java -jar target/pet-manager-1.0.0.jar --spring.profiles.active=mongo
```

---

### Docker

#### Build the image

Create a `Dockerfile` at the project root:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/pet-manager-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
mvn clean package -DskipTests
docker build -t pet-manager:latest .
docker run -p 8080:8080 pet-manager:latest
```

---

### Docker Compose — application + PostgreSQL

Create a `docker-compose.yml` at the project root:

```yaml
version: "3.9"

services:
  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: petdb
      POSTGRES_USER: petuser
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U petuser -d petdb"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: pet-manager:latest
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_USERNAME: petuser
      DB_PASSWORD: secret
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/petdb
    ports:
      - "8080:8080"
```

Start the stack:

```bash
docker compose up
```

---

### Docker Compose — application + MongoDB

```yaml
version: "3.9"

services:
  mongo:
    image: mongo:7
    ports:
      - "27017:27017"
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: pet-manager:latest
    depends_on:
      mongo:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: mongo
      MONGODB_URI: mongodb://mongo:27017/petdb
    ports:
      - "8080:8080"
```

---

### Kubernetes

A minimal Kubernetes deployment for the production (PostgreSQL) configuration:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: pet-manager
spec:
  replicas: 2
  selector:
    matchLabels:
      app: pet-manager
  template:
    metadata:
      labels:
        app: pet-manager
    spec:
      containers:
        - name: pet-manager
          image: pet-manager:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: prod
            - name: DB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: pet-manager-secrets
                  key: db-username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: pet-manager-secrets
                  key: db-password
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: pet-manager-svc
spec:
  selector:
    app: pet-manager
  ports:
    - port: 80
      targetPort: 8080
  type: ClusterIP
```

Create the Kubernetes secret:

```bash
kubectl create secret generic pet-manager-secrets \
  --from-literal=db-username=petuser \
  --from-literal=db-password=secret
kubectl apply -f k8s-deployment.yml
```

To switch to MongoDB on Kubernetes, change `SPRING_PROFILES_ACTIVE` to `mongo` and replace the `DB_*` environment variables with `MONGODB_URI`.

---

## Running Tests and Coverage Reports

### Run all tests

```bash
mvn test
```

### Run tests and enforce coverage threshold

```bash
mvn verify
```

The HTML coverage report is generated at:

```
target/site/jacoco/index.html
```

The build fails if line coverage drops below **90%** (excluding DTOs and the application entry point).

### Test structure

| Test class | Type | What it covers |
|---|---|---|
| `PetServiceImplTest` | Unit (Mockito) | Service layer business logic |
| `PetControllerTest` | Integration (MockMvc) | REST endpoints, security rules, validation |
| `JpaPetPersistenceAdapterTest` | Integration (@DataJpaTest) | JPA adapter with H2 |
| `MongoPetPersistenceAdapterTest` | Unit (Mockito) | MongoDB adapter conversions and delegation |
| `PetMapperTest` | Unit | DTO/entity mapping |
| `PetNotFoundExceptionTest` | Unit | Exception message |

