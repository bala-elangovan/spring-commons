package io.github.balaelangovan.spring.core.exception

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found status.
 */
class ResourceNotFoundException : DomainException {
    /**
     * @param message the error message describing what was not found
     */
    constructor(message: String) : super(ErrorCode.RESOURCE_NOT_FOUND, message)

    /**
     * @param message the error message describing what was not found
     * @param cause the underlying cause
     */
    constructor(message: String, cause: Throwable) : super(ErrorCode.RESOURCE_NOT_FOUND, message, cause)

    /**
     * @param resource the resource type that was not found
     * @param field the identifier field used in the lookup
     * @param message the error message
     */
    constructor(resource: String, field: String, message: String) : super(
        errorCode = ErrorCode.RESOURCE_NOT_FOUND,
        message = message,
        resource = resource,
        field = field,
    )
}
