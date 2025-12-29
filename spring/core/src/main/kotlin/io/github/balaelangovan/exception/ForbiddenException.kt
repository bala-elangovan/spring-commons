package io.github.balaelangovan.exception

/**
 * Exception thrown when access to a resource is forbidden.
 * Maps to HTTP 403 Forbidden status.
 */
class ForbiddenException : DomainException {
    /**
     * @param message the access denial reason
     */
    constructor(message: String) : super(ErrorCode.FORBIDDEN, message)

    /**
     * @param message the access denial reason
     * @param cause the underlying cause
     */
    constructor(message: String, cause: Throwable) : super(ErrorCode.FORBIDDEN, message, cause)

    /**
     * @param resource the resource being accessed
     * @param field the field related to the access denial
     * @param message the access denial reason
     */
    constructor(resource: String, field: String, message: String) : super(
        errorCode = ErrorCode.FORBIDDEN,
        message = message,
        resource = resource,
        field = field,
    )
}
