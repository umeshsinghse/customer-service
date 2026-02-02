# Customer Service API

A minimal Spring Boot 3.x application providing a simple REST API for customer management.

## Features

- **Spring Boot 3.x** with Java 17
- **Simple REST API** with hardcoded data (no database)
- **Docker support** with single-stage Dockerfile
- **GitLab CI/CD pipeline** with build, test, and docker-build stages

## API Endpoints

- `GET /api/customers` - Returns list of 3 hardcoded customers
- `GET /api/customers/{id}` - Returns single customer by ID
- `GET /health` - Simple health check returning `{"status": "UP"}`

## Customer Model

Simple customer object with:
- `id` (Long)
- `name` (String)
- `email` (String)

## Quick Start

### Prerequisites
- Java 17
- Maven 3.6+
- Docker (optional)

### Build and Run

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/customer-service-1.0.0-SNAPSHOT.jar

# Or run with Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Test the API

```bash
# Get all customers
curl http://localhost:8080/api/customers

# Get customer by ID
curl http://localhost:8080/api/customers/1

# Health check
curl http://localhost:8080/health
```

### Docker

```bash
# Build Docker image
docker build -t customer-service .

# Run with Docker Compose
docker-compose up
```

### Testing

```bash
# Run unit tests
mvn test
```

## Project Structure

```
customer-service/
├── src/
│   ├── main/java/com/example/customer/
│   │   ├── CustomerController.java
│   │   ├── CustomerService.java
│   │   ├── Customer.java
│   │   └── CustomerApplication.java
│   └── test/
│       └── java/com/example/customer/
│           └── CustomerServiceTest.java
├── Dockerfile
├── docker-compose.yml
├── .gitlab-ci.yml
├── pom.xml
└── README.md
```

## Dependencies

Minimal dependencies as specified:
- `spring-boot-starter-web` - Web framework
- `spring-boot-starter-test` - Testing framework

## GitLab CI/CD

The pipeline includes:
1. **Build stage** - Compile with Maven
2. **Test stage** - Run unit tests
3. **Docker build stage** - Build and push Docker image

Pipeline runs on `main` and `develop` branches.

## Configuration

- No database configuration required
- Uses Spring Boot defaults
- No additional application properties needed
- No logging configuration
- No security configuration