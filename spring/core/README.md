# Spring Core

Core common components shared across both WebMVC and WebFlux applications.

## Overview

This module provides framework-agnostic components that work with both traditional servlet-based and reactive Spring Boot applications. It contains shared exceptions, DTOs, constants, metrics collection, and base annotations.

## Features

### DTOs (Data Transfer Objects)

#### PageableDto
A lightweight DTO for pagination that works with Spring Data's `Pageable` interface:

```kotlin
data class PageableDto(
    val page: Int = 0,
    val size: Int = 20,
    val sort: List<String>? = null
)
```

**Usage:**
```kotlin
@GetMapping("/users")
fun getUsers(pageableDto: PageableDto): Page<User> {
    val pageable = pageableDto.toPageable()
    return userRepository.findAll(pageable)
}
```

#### ServiceError
Standardized error response structure:

```kotlin
data class ServiceError(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
```

### Exception Types

Pre-built exception hierarchy for common HTTP error scenarios:

| Exception                   | HTTP Status | Use Case                                  |
|-----------------------------|-------------|-------------------------------------------|
| `DomainException`           | 400         | Base exception for domain-specific errors |
| `ValidationException`       | 400         | Request validation failures               |
| `ResourceNotFoundException` | 404         | Resource not found                        |
| `ForbiddenException`        | 403         | Access denied                             |
| `ConflictException`         | 409         | Resource conflict (e.g., duplicate entry) |

**Usage:**
```kotlin
fun getUser(id: String): User {
    return userRepository.findById(id)
        ?: throw ResourceNotFoundException("User not found: $id")
}

fun createUser(user: User) {
    if (userRepository.existsByEmail(user.email)) {
        throw ConflictException("User with email ${user.email} already exists")
    }
    // ...
}
```

### Constants

#### HeaderConstants
Standard HTTP header names:

```kotlin
object HeaderConstants {
    const val X_USER_ID = "X-User-Id"
    const val X_USER_EMAIL = "X-User-Email"
    const val X_USER_GROUPS = "X-User-Groups"
    const val X_TRANSACTION_ID = "X-Transaction-Id"
    const val X_CLIENT_TRANSACTION_ID = "X-Client-Transaction-Id"
    const val X_CLIENT_ID = "X-Client-Id"
    const val X_API_KEY = "X-API-Key"
    const val X_SERVICE_ID = "X-Service-Id"
    const val X_FORWARDED_FOR = "X-Forwarded-For"
    const val AUTHORIZATION = "Authorization"
}
```

#### MdcKeys
MDC (Mapped Diagnostic Context) keys for distributed tracing:

```kotlin
object MdcKeys {
    const val TRANSACTION_ID = "transactionId"
    const val USER_ID = "userId"
    const val CLIENT_ID = "clientId"
    const val SERVICE_ID = "serviceId"
}
```

**Usage:**
```kotlin
import org.slf4j.MDC

MDC.put(MdcKeys.TRANSACTION_ID, UUID.randomUUID().toString())
log.info("Processing request") // Will include transactionId in logs
```

### Metrics Collection

Framework-agnostic metrics collection using Micrometer:

#### MetricsCollector Interface
```kotlin
interface MetricsCollector {
    fun incrementCounter(name: String, tags: Map<String, String> = emptyMap())
    fun recordGauge(name: String, value: Double, tags: Map<String, String> = emptyMap())
    fun recordTimer(name: String, duration: Duration, tags: Map<String, String> = emptyMap())
}
```

#### Usage
```kotlin
@Service
class UserService(private val metricsCollector: MetricsCollector) {

    fun createUser(user: User): User {
        val startTime = Instant.now()

        try {
            val created = userRepository.save(user)
            metricsCollector.incrementCounter(
                "user.created",
                mapOf("status" to "success")
            )
            return created
        } catch (e: Exception) {
            metricsCollector.incrementCounter(
                "user.created",
                mapOf("status" to "error")
            )
            throw e
        } finally {
            metricsCollector.recordTimer(
                "user.create.duration",
                Duration.between(startTime, Instant.now())
            )
        }
    }
}
```

### Base Annotations

#### @BaseErrorResponse
Swagger/OpenAPI annotation for consistent error responses:

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponses(
    ApiResponse(responseCode = "400", description = "Bad Request"),
    ApiResponse(responseCode = "401", description = "Unauthorized"),
    ApiResponse(responseCode = "403", description = "Forbidden"),
    ApiResponse(responseCode = "404", description = "Not Found"),
    ApiResponse(responseCode = "500", description = "Internal Server Error")
)
annotation class BaseErrorResponse
```

**Usage:**
```kotlin
@RestController
@RequestMapping("/api/users")
@BaseErrorResponse  // Apply to all endpoints in this controller
class UserController {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): User {
        // Auto-documented error responses
        return userService.getUser(id)
    }
}
```

#### @BaseAuthController
Combines common annotations for authenticated endpoints:

```kotlin
@RestController
@BaseAuthController
@RequestMapping("/api/admin")
class AdminController {
    // All endpoints require authentication
}
```

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    api("io.github.platform:spring-core:1.0.0")
}
```

## Dependencies

This module depends on:
- Spring Data Commons (for Pageable support)
- Jackson (for JSON serialization)
- SLF4J (for logging)
- Micrometer Core (for metrics)
- Swagger Annotations (optional, for API documentation)

## Module Structure

```
spring-core/
├── annotation/          # Base annotations
│   ├── BaseAuthController.kt
│   └── BaseErrorResponse.kt
├── constants/          # Constants
│   ├── HeaderConstants.kt
│   └── MdcKeys.kt
├── dto/                # Data Transfer Objects
│   ├── PageableDto.kt
│   └── ServiceError.kt
├── exception/          # Exception types
│   ├── ConflictException.kt
│   ├── DomainException.kt
│   ├── ErrorCode.kt
│   ├── ForbiddenException.kt
│   ├── ResourceNotFoundException.kt
│   └── ValidationException.kt
└── metrics/            # Metrics collection
    ├── MetricsCollector.kt
    ├── MicrometerMetricsCollector.kt
    └── config/
        └── MetricsAutoConfiguration.kt
```

## Configuration

### Metrics Auto-Configuration

Metrics are automatically configured if Micrometer is on the classpath. No additional configuration needed!

```yaml
# Optional: Configure Micrometer
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

## Best Practices

### Exception Handling

1. **Use specific exceptions** - Don't use generic exceptions
2. **Include context** - Provide helpful error messages
3. **Let global handlers catch them** - Don't catch and swallow exceptions

```kotlin
// ✅ Good
throw ResourceNotFoundException("User not found: $userId")

// ❌ Bad
throw RuntimeException("Error")
```

### MDC Usage

1. **Always clean up** - MDC values can leak across requests
2. **Use try-finally** - Ensure cleanup even on exceptions

```kotlin
// ✅ Good
try {
    MDC.put(MdcKeys.USER_ID, userId)
    processRequest()
} finally {
    MDC.remove(MdcKeys.USER_ID)
}
```

### Metrics

1. **Use consistent naming** - Follow Micrometer conventions (`service.action.metric`)
2. **Add relevant tags** - Makes filtering easier
3. **Don't over-instrument** - Focus on business-critical operations

## See Also

- [spring-boot-webmvc](../spring-boot-webmvc) - WebMVC-specific components
- [spring-boot-webflux](../spring-boot-webflux) - WebFlux-specific components
