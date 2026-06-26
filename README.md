# Blog Management System

A robust, production-quality Java Spring Boot 3.x REST API blogging platform built using Java 22, Maven, Spring Security, JWT authentication, MySQL, and OpenAPI.

---

## Features
* **Stateless JWT Authentication**: Secure, state-free authorization.
* **Role-Based Access Control (RBAC)**: Supports roles `USER` and `ADMIN`.
* **Blog Posts CRUD**:
  * Any visitor can read the posts.
  * Only registered users can create blog posts.
  * Only the owner can edit a blog post.
  * The owner or an `ADMIN` can delete a blog post.
* **N+1 Query Prevention**: Eager association fetching using `JOIN FETCH` queries.
* **Global Error Handler**: Unified JSON exception mapping (`@ControllerAdvice`).
* **Validation**: Input bean validation on request schemas.
* **API Documentation**: Automatic Swagger UI/OpenAPI document generations.
* **Actuator Metrics**: Health check endpoints and Prometheus scraping configured.
* **Docker Support**: Multi-stage `Dockerfile` and automated `docker-compose` orchestration.

---

## Project Structure
```text
com.blogmanagement/
├── config/       # SecurityConfig, OpenApiConfig, WebConfigs
├── controller/   # AuthController, BlogController
├── dto/          # Data Transfer Objects (Request/Response validation)
├── entity/       # User, BlogPost (JPA Entity annotations)
├── exception/    # Custom Exception files & GlobalExceptionHandler
├── repository/   # JpaRepository (UserRepository, BlogPostRepository)
├── security/     # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsService
├── service/      # AuthenticationService, BlogService, UserService
└── util/         # SecurityUtil principal extractors
```

---

## Getting Started

### Prerequisites
* **Java 22** installed.
* **Maven 3.8+** installed.
* **Docker** & **Docker Compose** (optional, for DB & containerized deployment).

---

### Method 1: Running Locally (Development Profile - H2)
By default, the application is configured to run with the `dev` profile using an **in-memory H2 database** requiring no installation.

1. Navigate to the project directory:
   ```bash
   cd JavaProject
   ```
2. Build and package the project:
   ```bash
   .\mvnw.cmd clean package
   ```
3. Run the application:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
4. The application will start at `http://localhost:8080`.
   * **Swagger UI API Docs**: `http://localhost:8080/swagger-ui/index.html`
   * **Prometheus Metrics scraping**: `http://localhost:8080/actuator/prometheus`
   * **H2 Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:blogdb`, Username: `sa`, Password: `password`)

---

### Method 2: Running with Docker Compose (Production Profile - MySQL)
This runs the application inside a container, backed by a MySQL database container.

1. Build and boot the stack:
   ```bash
   docker-compose up --build
   ```
2. The application container will wait for the MySQL container to be healthy before starting up on `http://localhost:8080`.
3. **Log levels**: Logs are written to stdout and also persisted inside the project directory under `./logs/blog-management.log`.

---

## Testing
Run the Mockito & JUnit 5 test suite:
```bash
.\mvnw.cmd clean test
```

---

## Sample Request Flows

### 1. Register User
`POST /api/v1/auth/register`
* **Request Body**:
  ```json
  {
      "name": "Jane Author",
      "email": "jane@example.com",
      "password": "password123"
  }
  ```
* **Response**:
  ```json
  {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tokenType": "Bearer",
      "user": {
          "id": 1,
          "name": "Jane Author",
          "email": "jane@example.com",
          "role": "USER"
      }
  }
  ```

### 2. Login User
`POST /api/v1/auth/login`
* **Request Body**:
  ```json
  {
      "email": "jane@example.com",
      "password": "password123"
  }
  ```
* **Response**:
  ```json
  {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "tokenType": "Bearer",
      "user": {
          "id": 1,
          "name": "Jane Author",
          "email": "jane@example.com",
          "role": "USER"
      }
  }
  ```

### 3. Create Blog Post (Authorized)
`POST /api/v1/blogs`
* **Headers**: `Authorization: Bearer <token>`
* **Request Body**:
  ```json
  {
      "title": "A Guide to Spring Boot 3",
      "content": "This is a detailed post about the new features introduced in Spring Boot 3..."
  }
  ```
* **Response**:
  ```json
  {
      "id": 1,
      "title": "A Guide to Spring Boot 3",
      "content": "This is a detailed post about the new features introduced in Spring Boot 3...",
      "createdAt": "2026-06-25T00:05:00",
      "updatedAt": "2026-06-25T00:05:00",
      "author": {
          "id": 1,
          "name": "Jane Author",
          "email": "jane@example.com",
          "role": "USER"
      }
  }
  ```

---

## OpenAPI / Swagger UI
* OpenAPI 3 Specs JSON: `http://localhost:8080/v3/api-docs`
* Swagger UI HTML: `http://localhost:8080/swagger-ui/index.html`
