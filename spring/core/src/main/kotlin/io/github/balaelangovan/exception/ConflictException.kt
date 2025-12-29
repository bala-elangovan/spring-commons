package io.github.balaelangovan.exception

/**
 * Exception thrown when a resource conflict is detected.
 * Maps to HTTP 409 Conflict status.
 */
class ConflictException : DomainException {
    /**
     * @param message the conflict description
     */
    constructor(message: String) : super(ErrorCode.CONFLICT, message)

    /**
     * @param message the conflict description
     * @param cause the underlying cause
     */
    constructor(message: String, cause: Throwable) : super(ErrorCode.CONFLICT, message, cause)

    /**
     * @param resource the resource type with the conflict
     * @param field the field causing the conflict
     * @param message the conflict description
     */
    constructor(resource: String, field: String, message: String) : super(
        errorCode = ErrorCode.CONFLICT,
        message = message,
        resource = resource,
        field = field,
    )
}
