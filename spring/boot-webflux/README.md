# Spring Boot WebFlux

Components for reactive Spring Boot WebFlux applications.

## Overview

This module provides production-ready components for building reactive Spring Boot WebFlux applications with Project Reactor. It includes auto-configurations for logging, security, exception handling, and a reactive REST client with OAuth2 support.

## Modules

### [starter](./starter)
Main starter module with auto-configurations:
- **Logging**: Reactive request/response logging filter
- **Security**: Reactive method-level authorization with `@Authorization` annotation
- **Exception Handling**: Global exception handler for reactive controllers

### [rest-client](./rest-client)
Reactive REST client with OAuth2 support:
- `AbstractReactiveRestClient` base class for building reactive REST clients
- Automatic OAuth2 token management (reactive)
- Token caching and refresh
- Header propagation

### [test-support](./test-support)
Testing utilities for WebFlux applications

## Quick Start

### Installation

Add the starter dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.platform:spring-boot-webflux-starter:1.0.0")
}
```

For REST client support, also add:

```kotlin
dependencies {
    implementation("io.github.platform:spring-boot-webflux-rest-client:1.0.0")
}
```

### Basic Usage

```kotlin
@SpringBootApplication
class MyApplication

fun main(args: Array<String>) {
    runApplication<MyApplication>(*args)
}
```

Auto-configuration handles everything!

## Features

### Authorization

Reactive method-level access control using the `@Authorization` annotation:

```kotlin
@RestController
@RequestMapping("/api/admin")
class AdminController {

    @Authorization(authorizedGroups = ["admin"])
    @GetMapping("/users")
    fun getAllUsers(exchange: ServerWebExchange): Flux<User> {
        // ServerWebExchange automatically injected for authorization check
        return userService.findAll()
    }

    @Authorization(authorizedGroups = ["admin", "manager"])
    @GetMapping("/reports")
    fun getReports(): Mono<List<Report>> {
        // Works without ServerWebExchange too (reads from reactive context)
        return reportService.getReports()
    }
}
```

**How it works:**
- Reads user groups from `X-User-Groups` header
- Supports both `ServerWebExchange` parameter and reactive context
- Works with `Mono` and `Flux` return types
- Throws `ForbiddenException` (403) if unauthorized

**Reactive Context:**
```kotlin
@GetMapping("/data")
fun getData(): Mono<Data> {
    // Authorization checked from reactive context
    return dataService.fetchData()
}
```

### Logging

Automatic reactive request/response logging:

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): Mono<User> {
        // Logs automatically include:
        // - Transaction ID
        // - User ID (from headers)
        // - Request method and path
        // - Response status
        log.info("Getting user: {}", id)
        return userService.getUser(id)
    }
}
```

**Log output example:**
```
[transactionId=abc-123] [userId=user@example.com] Getting user: 12345
```

### Exception Handling

Global exception handler for reactive controllers:

```kotlin
@Service
class UserService {
    fun getUser(id: String): Mono<User> {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(ResourceNotFoundException("User not found: $id")))
    }

    fun createUser(user: User): Mono<User> {
        return Mono.just(user)
            .filter { it.email.isValidEmail() }
            .switchIfEmpty(Mono.error(ValidationException("Invalid email format")))
            .flatMap { userRepository.save(it) }
    }
}
```

**Automatic HTTP Response:**
```json
{
  "timestamp": "2025-11-02T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "User not found: 12345",
  "path": "/api/users/12345"
}
```

### Reactive REST Client

Build type-safe reactive REST clients with OAuth2 support:

```kotlin
@Service
class UserServiceClient(
    webClient: WebClient,
    @Value("\${user-service.base-url}") baseUrl: String
) : AbstractReactiveRestClient(webClient, baseUrl) {

    fun getUser(id: String): Mono<User> {
        return get("/users/$id")
    }

    fun createUser(user: User): Mono<User> {
        return post("/users", user)
    }

    fun updateUser(id: String, user: User): Mono<User> {
        return put("/users/$id", user)
    }

    fun deleteUser(id: String): Mono<Void> {
        return delete("/users/$id")
    }

    fun getAllUsers(): Flux<User> {
        return get<List<User>>("/users").flatMapIterable { it }
    }
}
```

**Features:**
- Fully reactive (non-blocking)
- Automatic OAuth2 token management
- Token caching and refresh
- Header propagation
- Supports `Mono` and `Flux` return types
- Built-in error handling

### Advanced REST Client Usage

**With custom headers:**
```kotlin
fun getUserWithHeaders(id: String): Mono<User> {
    return get("/users/$id", mapOf("X-Custom-Header" to "value"))
}
```

**Handling empty responses:**
```kotlin
fun getOptionalUser(id: String): Mono<User> {
    return get<User>("/users/$id")
        .switchIfEmpty(Mono.empty())  // Returns empty Mono instead of error
}
```

**Error handling:**
```kotlin
fun getUser(id: String): Mono<User> {
    return get<User>("/users/$id")
        .onErrorResume(WebClientResponseException.NotFound::class.java) {
            Mono.error(ResourceNotFoundException("User not found: $id"))
        }
}
```

## Configuration

### Security Configuration

```yaml
platform:
  security:
    enabled: true  # Enable/disable authorization (default: true)
```

### OAuth2 REST Client Configuration

```yaml
platform:
  rest-client:
    oauth:
      enabled: true
      token-url: https://auth.example.com/oauth/token
      client-id: ${CLIENT_ID}
      client-secret: ${CLIENT_SECRET}
      grant-type: client_credentials
      scope: read write  # Optional
```

### WebClient Configuration

```yaml
spring:
  webflux:
    base-path: /api  # Optional API base path
```

### Logging Configuration

```yaml
logging:
  level:
    io.github.balaelangovan: DEBUG
    io.github.balaelangovan.filter.LoggingFilter: TRACE  # Detailed logs
    reactor.netty.http.client: DEBUG  # WebClient logs
```

## Advanced Usage

### Custom WebClient Bean

```kotlin
@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                )
            )
            .filter(/* custom filters */)
            .defaultHeader(HttpHeaders.USER_AGENT, "MyApp/1.0")
            .build()
    }
}
```

### Custom Authorization

```kotlin
@Authorization(
    authorizedGroups = ["admin"],
    headerNames = ["X-User-Roles", "X-Groups"],  // Check multiple headers
    delimiter = "|"  // Custom delimiter
)
```

### Reactive Context Propagation

```kotlin
@GetMapping("/data")
fun getData(exchange: ServerWebExchange): Mono<Data> {
    return dataService.fetchData()
        .contextWrite(Context.of(ServerWebExchange::class.java, exchange))
}
```

### Disable Features

```yaml
# Disable authorization
platform:
  security:
    enabled: false

# Disable OAuth for REST client
platform:
  rest-client:
    oauth:
      enabled: false
```

## Module Structure

```
spring-boot-webflux/
├── starter/
│   ├── annotation/
│   │   └── BaseWebFluxApp.kt
│   ├── config/
│   │   ├── ExceptionAutoConfiguration.kt
│   │   ├── LoggingAutoConfiguration.kt
│   │   └── SecurityAutoConfiguration.kt
│   ├── exception/
│   │   └── GlobalExceptionHandler.kt
│   ├── filter/
│   │   └── LoggingFilter.kt
│   └── security/
│       ├── Authorization.kt
│       └── AuthorizationAspect.kt
├── rest-client/
│   ├── client/
│   │   ├── AbstractReactiveRestClient.kt
│   │   └── oauth/
│   │       ├── ReactiveOAuthTokenManager.kt
│   │       └── OAuthProperties.kt
│   └── config/
│       └── RestClientAutoConfiguration.kt
└── test-support/
```

## Best Practices

### Reactive Programming

1. **Never block** - Avoid blocking operations (`block()`, `blockFirst()`, `toIterable()`)
2. **Proper error handling** - Use `onErrorResume()`, `onErrorReturn()`, `doOnError()`
3. **Backpressure** - Let Reactor handle backpressure automatically

```kotlin
// ✅ Good
fun getUsers(): Flux<User> {
    return userRepository.findAll()
        .onErrorResume { error ->
            log.error("Error fetching users", error)
            Flux.empty()
        }
}

// ❌ Bad - blocks the reactive pipeline
fun getUsers(): List<User> {
    return userRepository.findAll().collectList().block()!!  // DON'T DO THIS
}
```

### Authorization

1. **Pass ServerWebExchange when possible** - More efficient
2. **Use reactive context for nested calls** - Context propagates automatically
3. **Consistent group naming** - Use standardized group names

### REST Client

1. **Connection pooling** - WebClient reuses connections automatically
2. **Timeouts** - Configure response timeouts to prevent hanging requests
3. **Error handling** - Handle 4xx and 5xx responses appropriately

```kotlin
fun getUser(id: String): Mono<User> {
    return get<User>("/users/$id")
        .timeout(Duration.ofSeconds(5))  // Timeout after 5 seconds
        .retry(2)  // Retry twice on failure
        .onErrorResume(WebClientResponseException::class.java) { error ->
            log.error("Failed to fetch user: {}", id, error)
            Mono.error(ResourceNotFoundException("User not found"))
        }
}
```

### Testing

```kotlin
@WebFluxTest(UserController::class)
class UserControllerTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `should get user`() {
        webTestClient.get()
            .uri("/api/users/{id}", "123")
            .exchange()
            .expectStatus().isOk
            .expectBody<User>()
            .consumeWith { response ->
                assertThat(response.responseBody?.id).isEqualTo("123")
            }
    }
}
```

## Troubleshooting

### Authorization Not Working

**Problem:** `@Authorization` annotation is ignored

**Solution:**
1. Ensure `platform.security.enabled=true`
2. Verify `X-User-Groups` header is present
3. Check method returns `Mono` or `Flux` (not blocking types)

### OAuth Token Not Being Added

**Problem:** REST client requests don't include OAuth token

**Solution:**
1. Enable OAuth: `platform.rest-client.oauth.enabled=true`
2. Configure token URL and credentials
3. Verify `ReactiveOAuthTokenManager` bean exists

### Memory Leaks

**Problem:** Application memory grows over time

**Solution:**
- Never call `.block()` in reactive chains
- Always subscribe to returned `Mono`/`Flux`
- Use `.share()` for hot publishers

### Context Lost

**Problem:** ServerWebExchange not found in reactive context

**Solution:**
- Pass `ServerWebExchange` as method parameter
- Use `.contextWrite()` to propagate context manually

## Performance Tips

1. **Use `Flux.defer()` for cold publishers** - Creates new stream for each subscriber
2. **Cache frequently accessed data** - Use `Mono.cache()` or `Flux.cache()`
3. **Batch operations** - Use `.buffer()` to batch operations
4. **Parallel processing** - Use `.parallel()` for CPU-intensive operations

```kotlin
fun processUsers(): Flux<ProcessedUser> {
    return userRepository.findAll()
        .buffer(100)  // Process in batches of 100
        .flatMap { batch ->
            Flux.fromIterable(batch)
                .parallel()
                .runOn(Schedulers.parallel())
                .map { user -> processUser(user) }
                .sequential()
        }
}
```

## See Also

- [spring-core](../spring-core) - Core components
- [spring-boot-webmvc](../spring-boot-webmvc) - Servlet-based alternative
- [Project Reactor Documentation](https://projectreactor.io/docs/core/release/reference/)
