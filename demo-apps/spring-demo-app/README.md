# Spring Boot Demo App

Simple Spring Boot REST API for testing Jenkins CI/CD pipeline with shared library.

## Features

- Spring Boot 3.2.0
- Java 17
- REST API endpoints
- Spring Boot Actuator for health checks
- Unit tests with JUnit 5
- Gradle build system
- Docker multi-stage build
- Jenkins pipeline integration

## API Endpoints

- `GET /` - Home page with app info
- `GET /api/info` - Application information
- `GET /api/health` - Health check endpoint
- `GET /actuator/health` - Spring Boot Actuator health
- `GET /actuator/info` - Spring Boot Actuator info

## Local Development

```bash
./gradlew bootRun
```

## Build

```bash
./gradlew build
```

## Test

```bash
./gradlew test
```

## Docker Build

```bash
docker build -t chanserey/spring-demo-app:latest .
```

## Jenkins Pipeline

This project includes a Jenkinsfile that uses the shared library for:
- Running unit tests
- Building JAR file
- Building Docker images
- Pushing to Docker Hub
- Deploying containers
- Telegram notifications
