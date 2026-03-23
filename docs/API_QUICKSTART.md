# API Quickstart (2-5 minutes)

This guide lets you verify that the API is really working end-to-end with minimal setup.

## What is included

- `scripts/api-smoke-test.sh`: automatic smoke test for create/read/update/delete.
- `api/pet-manager.http`: ready-to-run HTTP requests for VS Code REST Client.
- `api/pet-manager.postman_collection.json`: ready-to-import Postman collection.

## Prerequisites

- Java 17+
- Maven 3.9+

If your shell is still using Java 11, set Java 17 for the current session:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -version
```

## 1) Start the application

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

## 2) Run the smoke test script

In a new terminal:

```bash
chmod +x scripts/api-smoke-test.sh
./scripts/api-smoke-test.sh
```

Expected result:

- all checks print `[OK]`
- final line: `Smoke test completed successfully.`

### Optional: custom URL or credentials

```bash
BASE_URL=http://localhost:8080 \
ADMIN_USER=admin ADMIN_PASS=admin123 \
USER_USER=user USER_PASS=user123 \
./scripts/api-smoke-test.sh
```

## 3) Try requests manually in VS Code

Open `api/pet-manager.http`.

If you use the REST Client extension, run each request with "Send Request".

- Create pet (ADMIN)
- Get all pets (USER)
- Get by id (USER)
- Update (ADMIN)
- Delete (ADMIN)

Before GET/PUT/DELETE by id, set `@petId` to an existing id.

## 4) Try requests with Postman

1. Open Postman.
2. Import file: `api/pet-manager.postman_collection.json`.
3. Run `Create Pet (ADMIN)` first.
4. Run the remaining requests in order.

The collection stores the created id into `petId` automatically after `Create Pet (ADMIN)`.

## 5) Optional CLI run with Newman

If you want to execute the Postman collection from terminal:

```bash
npm install -g newman
newman run api/pet-manager.postman_collection.json
```

## Default credentials

- USER: `user` / `user123` (read-only)
- ADMIN: `admin` / `admin123` (full access)

## Common failures

- `401 Unauthorized`: missing/wrong credentials.
- `403 Forbidden`: USER trying to POST/PUT/DELETE.
- `404 Not Found`: id does not exist.
- `400 Bad Request`: validation failed (`name`/`species` blank, `age < 0`).
