# Book Spring Boot Demo

Spring Boot API for managing students, books, book reservations, authentication, and student photo uploads.

## Requirements

- Docker Desktop
- Docker Compose
- Java 17, only needed if running WITHOUT Docker
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

## Initialize Or Reset Database Schema

The app uses `spring.jpa.hibernate.ddl-auto=none`, so tables must exist in MySQL.

To apply the schema to or reset the database in the running MySQL container:

```bash
docker exec -i book-demo-mysql mysql -u root -ppassword < src/main/resources/db/schema.sql
```

To populate dummy book data in the database (make sure there is the database):

```bash
docker exec -i book-demo-mysql mysql -u root -ppassword < src/main/resources/db/books.sql
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Kubernetes

This demo is containerized with Docker Compose. Kubernetes deployment is not included yet, but the services are structured so the Spring Boot app, reservation report service, and MySQL database can be deployed as separate Kubernetes workloads.

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
- Student numbers must be at least 3 digits long and unique.
- Emails must use a valid email format and be unique.
- Phone numbers must be 9 to 10 characters long and unique.
- Uploaded photos are limited to `10MB`.
- Only JPEG and PNG photo uploads are accepted.
- Access token lifespan is set by `ACCESS_TOKEN_DAYS`; development defaults to 3 days.
- Refresh token lifespan is set by `REFRESH_TOKEN_DAYS`; development defaults to 7 days.
- Microservice communication demo: `GET /api/book/get/reservations/report` calls the reservation report service.
