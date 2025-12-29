package io.github.balaelangovan.spring.core.exception

/**
 * Abstract base exception for all domain-specific exceptions.
 * Provides structured error information including error code, resource, and field details.
 *
 * @param errorCode the error code classification
 * @param message the error message
 * @param cause the underlying cause
 * @param resource the resource type where the error occurred
 * @param field the specific field that caused the error
 */
abstract class DomainException(
    val errorCode: ErrorCode,
    message: String,
    cause: Throwable? = null,
    val resource: String? = null,
    val field: String? = null,
) : RuntimeException(message, cause)
