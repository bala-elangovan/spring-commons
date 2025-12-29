package io.github.balaelangovan.annotation

import io.github.balaelangovan.dto.ServiceError
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.MediaType

/**
 * Composite annotation for documenting standard error responses in API documentation.
 * Automatically adds common HTTP error response codes with ServiceError schema to OpenAPI documentation.
 *
 * Applied error responses:
 * - 400 Bad Request: Invalid request parameters or body
 * - 401 Unauthorized: Missing or invalid authentication
 * - 403 Forbidden: Insufficient permissions
 * - 404 Not Found: Resource not found
 * - 409 Conflict: Resource conflict (duplicate, state conflict)
 * - 422 Unprocessable Entity: Validation failed
 * - 500 Internal Server Error: Unexpected server error
 *
 * Usage:
 * ```kotlin
 * @GetMapping("/users/{id}")
 * @CommerceErrorResponse
 * fun getUser(@PathVariable id: String): UserResponse {
 *     // ... implementation
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid request parameters or malformed request body",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid authentication credentials",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions to access the resource",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "404",
            description = "Not Found - Requested resource does not exist",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "409",
            description = "Conflict - Resource already exists or operation conflicts with current state",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "422",
            description = "Unprocessable Entity - Validation failed for one or more fields",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
        ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - An unexpected error occurred on the server",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ServiceError::class),
                ),
            ],
        ),
    ],
)
annotation class BaseErrorResponse
