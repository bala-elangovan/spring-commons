package io.github.balaelangovan.spring.core.exception

/**
 * Exception thrown when validation fails.
 * Maps to HTTP 400 Bad Request status.
 */
class ValidationException : DomainException {
    /**
     * @param message the validation error message
     */
    constructor(message: String) : super(ErrorCode.VALIDATION_FAILED, message)

    /**
     * @param message the validation error message
     * @param cause the underlying cause
     */
    constructor(message: String, cause: Throwable) : super(ErrorCode.VALIDATION_FAILED, message, cause)

    /**
     * @param resource the resource type being validated
     * @param field the field that failed validation
     * @param message the validation error message
     */
    constructor(resource: String, field: String, message: String) : super(
        errorCode = ErrorCode.VALIDATION_FAILED,
        message = message,
        resource = resource,
        field = field,
    )
}
