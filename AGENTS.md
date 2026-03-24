# AGENTS.md - AI Agent Guidance for LearningSpringBoot

## Architecture Overview

This is a **Spring Boot 4.0.3** REST API with **dual persistence layer support**, JWT-based authentication, and role-based access control. Key architectural decision: the codebase abstracts persistence to support switching between JPA and JDBC Template implementations via `app.persistence.enableJPA` property (currently disabled, using JDBC).

**Core Components**:
- **Controllers** (`AuthController`, `StudentController`): REST endpoints with `@PreAuthorize` method-level security
- **Services** (interface + implementation): Business logic layer with ModelMapper DTOs
- **Repository Layer**: Abstract `StudentRepository` & `UserRepository` interfaces with conditional bean selection
  - **JDBC Path** (enabled by default): `StudentDAO` uses `JdbcTemplate` + `StudentRowMapper`
  - **JPA Path** (conditional): `JpaStudentRepository` extends `JpaRepository`
- **Security**: JWT tokens (24h expiration), `JwtAuthenticationFilter`, `SecurityConfig` with role-based endpoints
- **Exception Handling**: `GlobalExceptionHandler` with custom exceptions (`ResourceNotFoundException`, `BadRequestException`, `DuplicateResourceException`)
- **DTOs**: Value objects for API contracts (`StudentDTO`, `AddStudentDTO`, `LoginRequestDTO`, `LoginResponseDTO`)

## Critical Data Flows

1. **Authentication Flow**: `/auth/login` → `AuthenticationManager` → `JwtUtil.generateToken()` → Client stores JWT
2. **Student CRUD**: Controller → Service → `StudentRepository` (interface) → `StudentDAO` (JDBC) or `JpaStudentRepository` (JPA) → PostgreSQL
3. **Partial Updates**: `PATCH /student/{id}` accepts `Map<String, Object>` and validates each field before updating

## Essential Patterns & Conventions

### Repository Pattern with Conditional Loading
```java
@Repository
@ConditionalOnProperty(name = "app.persistence.enableJPA", havingValue = "false", matchIfMissing = true)
public class StudentDAO implements StudentRepository { ... }
```
**Convention**: When adding persistence methods, implement on BOTH `StudentDAO` (JDBC) AND `JpaStudentRepository` (JPA). Always use the interface contract in services.

### DTO Mapping via ModelMapper
```java
// In service implementation
Student student = studentRepository.findById(id)...
return modelMapper.map(student, StudentDTO.class);
```
**Convention**: DTOs are always returned to clients, never entities. ModelMapper is configured in `MapperConfig`.

### Security Annotations
```java
@PreAuthorize("hasRole('ADMIN')")  // For POST/PUT/DELETE/PATCH on students
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")  // For GET operations
```
**Convention**: Roles stored as `ROLE_USER` and `ROLE_ADMIN` in User entity, extracted by `SecurityConfig.userDetailsService()`.

### Exception Handling
Custom exceptions are caught globally in `GlobalExceptionHandler`. Always throw semantic exceptions from services:
- `ResourceNotFoundException`: When record doesn't exist
- `DuplicateResourceException`: On constraint violations (e.g., duplicate email)
- `BadRequestException`: Invalid input or invalid field names in PATCH

## Developer Workflows

### Build & Run
```powershell
mvn clean compile          # Compile with Lombok annotation processing
mvn spring-boot:run        # Start server on http://localhost:8080
mvn test                   # Run tests (Maven Surefire)
```

### Database Setup
- **DB**: PostgreSQL (Supabase pooler) at `aws-1-ap-northeast-1.pooler.supabase.com:5432`
- **Credentials**: In `application.properties` (hardcoded - security risk)
- **DDL**: `spring.jpa.hibernate.ddl-auto=update` (only used if JPA enabled)

### Testing Endpoints
Postman collection included: `PostmanCollection.json`. Key routes:
- `POST /auth/login` → Returns JWT token
- `GET /student` → List all (requires auth)
- `POST /student` → Create (ADMIN only)
- `PATCH /student/{id}` → Partial update (ADMIN only) with `{"name": "...", "email": "..."}`

### Switching Persistence Layer
Edit `application.properties`:
- Set `app.persistence.enableJPA=false` → Uses `StudentDAO` (default)
- Set `app.persistence.enableJPA=true` → Uses `JpaStudentRepository`
Both automatically wire to services via Spring's conditional bean loading.

## Cross-Component Communication

- **Controllers** → Services via `StudentService` interface (injected as constructor dependency via `@RequiredArgsConstructor`)
- **Services** → Repository interfaces (not implementations - enables swapping)
- **Security Filter** → `JwtUtil` for token validation on every request
- **Security Config** → `UserRepository` for loading `UserDetailsService`

## Project-Specific Gotchas

1. **Dual Persistence Support**: Adding new methods? Implement on both `StudentDAO.java` AND update the interface + `JpaStudentRepository`. Tests must pass for both configurations.
2. **Lombok Annotations**: `@Data`, `@RequiredArgsConstructor`, `@Slf4j` require annotation processor. Maven compiler already configured; don't break it.
3. **Email Uniqueness**: Both JDBC and JPA must enforce unique email constraint. Catches `DataIntegrityViolationException` in `GlobalExceptionHandler`.
4. **Hardcoded JWT Secret**: `JwtUtil.SECRET = "MySecretKeyForJWTTokenGenerationThatIsAtLeast32Characters"` - move to properties for production.
5. **Role Format**: Users stored as `"ROLE_USER"` in DB, but `SecurityConfig.userDetailsService()` strips `"ROLE_"` prefix before passing to Spring Security. Tokens embed full `"ROLE_USER"` string.

## File Organization Reference

```
src/main/java/com/navansh/LearningSpringBoot/
├── Application.java              # Spring Boot entry point
├── config/MapperConfig.java       # ModelMapper bean
├── controller/                    # REST endpoints
├── service/impl/                  # Business logic
├── repository/                    # Repository interfaces + conditional implementations
├── dao/StudentDAO.java            # JDBC implementation
├── entity/                        # JPA entities
├── dto/                          # API contracts
├── exception/                     # Custom exceptions + GlobalExceptionHandler
├── security/                      # JWT filter + SecurityConfig
└── utils/JwtUtil.java            # Token generation/validation
```

When adding features, follow this layering: Controller → Service Interface → Service Implementation → Repository Interface → DAO/JPA Implementation.

