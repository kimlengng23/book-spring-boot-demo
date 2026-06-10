# Book Spring Boot Demo

Spring Boot API for managing students, books, book reservations, authentication, and student photo uploads.

## Requirements

- Docker Desktop
- Docker Compose
- Java 17, only needed if running without Docker
- Maven, or use the included Maven wrapper `./mvnw`

## Run With Docker

From the project root:

```bash
docker compose up --build
```

This starts:

- Spring Boot app: `http://localhost:8080`
- Reservation report service: `http://localhost:8081`
- MySQL container: `book-demo-mysql`
- MySQL host port: `3307`
- MySQL database: `BookAppDemo`
- MySQL username: `root`
- MySQL password: `password`

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Initialize Or Reset Database Schema

The app uses `spring.jpa.hibernate.ddl-auto=none`, so tables must exist in MySQL.

To apply the schema to the running MySQL container:

```bash
docker exec -i book-demo-mysql mysql -u root -ppassword < src/main/resources/db/schema.sql
```

To fully reset the database and re-run the schema:

```bash
docker exec -i book-demo-mysql mysql -u root -ppassword -e "DROP DATABASE IF EXISTS BookAppDemo;"
docker exec -i book-demo-mysql mysql -u root -ppassword < src/main/resources/db/schema.sql
```

Warning: dropping the database deletes all existing data.

## Run Locally Without Docker

Start MySQL first. If using the Docker MySQL container only:

```bash
docker compose up -d mysql
docker exec -i book-demo-mysql mysql -u root -ppassword < src/main/resources/db/schema.sql
```

Then run the app locally:

```bash
SPRING_PROFILES_ACTIVE=development \
DB_HOST=localhost \
DB_PORT=3307 \
DB_NAME=BookAppDemo \
DB_USERNAME=root \
DB_PASSWORD=password \
STUDENT_PHOTOS_DIR=./uploads/student-photos \
./mvnw spring-boot:run
```

## Build And Check

Compile without running tests:

```bash
./mvnw -DskipTests compile
```

Package the app:

```bash
./mvnw -DskipTests package
```

Run tests:

```bash
./mvnw test
```

## Useful Docker Commands

Stop containers:

```bash
docker compose down
```

Stop containers and delete volumes, including MySQL data:

```bash
docker compose down -v
```

View app logs:

```bash
docker compose logs -f app
```

View reservation report service logs:

```bash
docker compose logs -f reservation-report-service
```

View MySQL logs:

```bash
docker compose logs -f mysql
```

Open MySQL shell:

```bash
docker exec -it book-demo-mysql mysql -u root -ppassword BookAppDemo
```

## Notes

- Student photo uploads are stored in the Docker volume `student-photos`.
- Uploaded photos are limited to `10MB`.
- Only JPEG and PNG photo uploads are accepted.
- Access tokens expire after 15 minutes.
- Refresh tokens expire after 7 days.
- Microservice communication demo: `GET /api/book/reservations/report` calls the reservation report service.
