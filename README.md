# Spring Commons

A comprehensive Spring Boot 4.x library providing common components, utilities, and auto-configurations for both WebMVC and WebFlux applications.

## Overview

Spring Commons is a modular library that eliminates boilerplate code and provides battle-tested components for building robust Spring Boot microservices. It supports both traditional servlet-based (WebMVC) and reactive (WebFlux) applications with full Spring Boot 4.0 compatibility.

Built on top of gradle-plugins convention plugins, it provides consistent dependency management and build configuration across all modules.

## Architecture

This library uses centralized version management and build conventions:

### Build Configuration
- **gradle-plugins** - Provides convention plugins for consistent build configuration
  - `io.github.balaelangovan.java-conventions` - Used by all library modules (starter, rest-client, test-support)
  - Includes Spotless formatting, JaCoCo coverage, and common build settings
- **Gradle Version Catalog** (gradle/libs.versions.toml) - Minimal catalog with library-specific versions only
- **Spring Boot 4.0.1 BOM** - Manages all Spring ecosystem dependency versions

### Dependency Management
All modules explicitly apply Spring Boot's dependency management plugin to ensure consistent versions across the library. Inter-module dependencies use Gradle project references to avoid version conflicts during build.

## Modules

### [spring/core](./spring/core)
Core components shared across both WebMVC and WebFlux:
- Common exceptions and error handling
- DTOs (PageableDto, ServiceError)
- Constants (headers, MDC keys)
- Metrics collection
- Base annotations

### [spring/boot-webmvc](./spring/boot-webmvc)
Components for traditional Spring Boot WebMVC applications:
- **starter**: Auto-configuration for logging, security, exception handling, async support
- **rest-client**: RestTemplate-based REST client with OAuth2 support
- **test-support**: Testing utilities for WebMVC applications

### [spring/boot-webflux](./spring/boot-webflux)
Components for reactive Spring Boot WebFlux applications:
- **starter**: Auto-configuration for logging, security, exception handling
- **rest-client**: WebClient-based reactive REST client with OAuth2 support
- **test-support**: Testing utilities for WebFlux applications

## Quick Start

### Installation

Add the appropriate starter dependency to your `build.gradle.kts`:

**For WebMVC applications:**
```kotlin
dependencies {
    implementation("io.github.balaelangovan:spring-boot-webmvc-starter:1.0.0")
}
```

**For WebFlux applications:**
```kotlin
dependencies {
    implementation("io.github.balaelangovan:spring-boot-webflux-starter:1.0.0")
}
```

### Usage

Simply add the dependency - auto-configuration handles the rest!

```kotlin
@SpringBootApplication
class MyApplication

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}
```

## Features

### Security
- Authorization annotation for method-level access control
- Header-based authorization (X-User-Groups)
- Configurable authorization aspect with AspectJ weaver

### Logging
- **Logback included** - No need to add logging dependencies separately
- Pre-configured Logback setup included in spring-core (logback-spring.xml)
- Color-coded console output for local/dev profiles (errors in red, etc.)
- MDC (Mapped Diagnostic Context) support
- Transaction ID propagation and logging
- Request/Response logging filters
- Async-safe logging with MDC context propagation
- File-based logging with rolling policy for production profiles
- Profile-specific log configurations

### Exception Handling
- Global exception handlers for both WebMVC and WebFlux
- Standardized error responses
- Built-in exception types (ValidationException, ResourceNotFoundException, ForbiddenException, etc.)
- Proper HTTP status code mapping

### Metrics
- Micrometer-based metrics collection
- Auto-configuration for metrics
- Extensible metrics collector interface
- Framework-agnostic design

### REST Clients
- OAuth2-enabled REST clients
- Automatic token management and refresh
- Header propagation (transaction IDs, user context)
- Both sync (RestTemplate) and reactive (WebClient) implementations
- Type-safe request/response handling

### Configuration
- Externalized configuration support
- Sensible defaults
- Easy override via application.properties/yaml
- Spring Boot 4.x auto-configuration

## Configuration

### Security Configuration

```yaml
platform:
  security:
    enabled: true  # Enable/disable security features (default: true)
```

### REST Client OAuth Configuration

```yaml
platform:
  rest-client:
    oauth:
      enabled: true
      token-url: https://auth.example.com/oauth/token
      client-id: your-client-id
      client-secret: your-client-secret
      grant-type: client_credentials
      scope: read write
```

### Logging Configuration

The library includes Logback as a transitive dependency and provides a pre-configured `logback-spring.xml` in the spring-core module. No additional logging dependencies are required!

**Local/Dev Profile Features:**
- Color-coded console output (errors in red, warnings in yellow, etc.)
- Simplified log format for better readability
- Console-only logging (no file output)

**Production Profile Features:**
- Plain console output (no colors)
- File-based logging with rolling policy (10MB per file, 30 days retention)
- Async file appender for better performance
- MDC context included in logs (transactionId, userId, requestId)

**Activate profiles:**
```yaml
spring:
  profiles:
    active: local  # or dev, prod, etc.
```

**Override log levels:**
```yaml
logging:
  level:
    io.github.balaelangovan: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

**Customize log file location:**
```yaml
logging:
  file:
    path: /var/log/myapp
    name: myapp
```

## Examples

### Using Authorization

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController {

    @Authorization(authorizedGroups = ["admin", "manager"])
    @GetMapping
    fun getUsers(): List<User> {
        // Only users in "admin" or "manager" groups can access
        return userService.findAll()
    }
}
```

### Using REST Client (WebMVC)

```kotlin
@Service
class UserServiceClient(
    restTemplate: RestTemplate,
    oAuthTokenManager: OAuthTokenManager?
) : AbstractRestClient(restTemplate, oAuthTokenManager) {

    override fun getBaseUrl() = "https://api.example.com"

    fun getUser(id: String): User {
        return get("/users/$id", User::class.java).body!!
    }
}
```

### Using REST Client (WebFlux)

```kotlin
@Service
class UserServiceClient(
    webClient: WebClient,
    baseUrl: String
) : AbstractReactiveRestClient(webClient, baseUrl) {

    fun getUser(id: String): Mono<User> {
        return get("/users/$id")
    }
}
```

### Exception Handling

```kotlin
@Service
class UserService {
    fun getUser(id: String): User {
        return userRepository.findById(id)
            ?: throw ResourceNotFoundException("User not found: $id")
    }
}
```

The global exception handler automatically converts this to a proper HTTP response:
```json
{
  "timestamp": "2025-11-02T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found: 123",
  "path": "/api/users/123"
}
```

## Version Management

This library uses a minimal Gradle Version Catalog approach:

**gradle/libs.versions.toml** contains only library-specific versions:
```toml
[versions]
kotlin = "2.2.20"
spring-boot = "4.0.1"
swagger-annotations = "2.2.41"
platform-gradle-plugins = "1.0.0"
```

All other dependency versions are managed by:
- **Spring Boot 4.0.1 BOM** - Spring ecosystem dependencies
- **gradle-plugins** - Build tooling and common libraries
- **Kotlin plugin** - Kotlin standard library versions

This approach ensures:
- Single source of truth for versions
- No version duplication
- Automatic version alignment
- Easier maintenance and updates

## Development

### Prerequisites

1. Install gradle-plugins locally:
```bash
git clone https://github.com/bala-elangovan/gradle-plugins.git
cd gradle-plugins
./gradlew publishToMavenLocal
```

### Building

```bash
# Build all modules
./gradlew build

# Clean build
./gradlew clean build

# Build specific module
./gradlew :spring:core:build
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :spring:boot-webmvc:starter:test
```

### Code Quality

```bash
# Check code formatting (Spotless)
./gradlew spotlessCheck

# Auto-fix code formatting
./gradlew spotlessApply

# Run all checks (formatting + tests)
./gradlew check
```

### Publishing Locally

```bash
# Publish to Maven Local
./gradlew publishToMavenLocal
```

## Requirements

- **Java**: 21+
- **Spring Boot**: 4.0.1
- **Kotlin**: 2.2.20
- **Gradle**: 9.2.0+
- **gradle-plugins**: 1.0.0 (published to mavenLocal)

## Dependency Resolution

All library modules use the `io.github.balaelangovan.java-conventions` plugin from gradle-plugins and explicitly configure Spring Boot's dependency management:

**Module build configuration (starter, rest-client, test-support):**
```kotlin
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.github.balaelangovan.java-conventions")
}

// Apply Spring Boot's dependency management
apply(plugin = "io.spring.dependency-management")

val springBootVersion: String by project.extra {
    rootProject.libs.versions.spring.boot.get()
}

configure<DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
    }
}
```

**Inter-module dependencies use project references:**
```kotlin
dependencies {
    // Project dependencies
    api(project(":spring:core"))
    api(project(":spring:boot-webflux:starter"))

    // Spring Boot managed dependencies (no version needed)
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("tools.jackson.module:jackson-module-kotlin")

    // Custom versions from version catalog
    api(rootProject.libs.swagger.annotations)
}
```

This approach ensures:
- Consistent dependency versions across all modules
- Proper inter-module dependency resolution during build
- No circular dependencies or version conflicts

## Contributing

1. Ensure all tests pass: `./gradlew test`
2. Format code: `./gradlew spotlessApply`
3. Run all checks: `./gradlew check`
4. Follow conventional commit messages
5. Update documentation for new features

## License

MIT License - see [LICENSE](./LICENSE) file for details.

## Author

**Balamurugan Elangovan**

[GitHub](https://github.com/bala-elangovan) | [LinkedIn](https://www.linkedin.com/in/balamurugan-elangovan-53791985/) | mail.bala0224@gmail.com

## Support

For issues and questions, please open an issue on the GitHub repository.
