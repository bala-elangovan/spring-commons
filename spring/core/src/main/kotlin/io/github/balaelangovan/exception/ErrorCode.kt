package io.github.balaelangovan.exception

import org.springframework.http.HttpStatus

/**
 * Standard error codes with associated HTTP status codes and default messages.
 *
 * @param code unique string identifier for the error
 * @param httpStatus HTTP status code to return
 * @param defaultMessage default human-readable message
 */
enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    val defaultMessage: String,
) {
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Validation failed for the request"),
    INVALID_REQUEST("INVALID_REQUEST", HttpStatus.BAD_REQUEST, "Invalid request parameters"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "Requested resource not found"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "Access forbidden"),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "Resource conflict detected"),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
}
