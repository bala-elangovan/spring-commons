package io.github.balaelangovan.spring.webmvc.starter.exception

import io.github.balaelangovan.spring.core.constants.MdcKeys
import io.github.balaelangovan.spring.core.dto.ServiceError
import io.github.balaelangovan.spring.core.exception.DomainException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler that catches exceptions and converts them to standardized ServiceError responses.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * Handles domain exceptions with structured error information.
     *
     * @param ex The domain exception to handle.
     * @return ResponseEntity<ServiceError> with the error response.
     */
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException): ResponseEntity<ServiceError> {
        val errorCode = ex.errorCode
        log.error(DOMAIN_EXCEPTION_LOG, errorCode.code, ex.message, ex)

        val errorDetail =
            if (ex.resource != null || ex.field != null) {
                ServiceError.DetailedErrorMessage(
                    resource = ex.resource,
                    field = ex.field,
                    reason = ex.message ?: errorCode.defaultMessage,
                )
            } else {
                null
            }

        val serviceError =
            ServiceError(
                errorCode = errorCode.httpStatus.value() * 100 + errorCode.ordinal,
                message = ex.message ?: errorCode.defaultMessage,
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
                errorMessages = errorDetail?.let { listOf(it) },
            )

        return ResponseEntity.status(errorCode.httpStatus).body(serviceError)
    }

    /**
     * Handles validation errors from @Valid annotation.
     *
     * @param ex The validation exception to handle.
     * @return Mono<ResponseEntity<ServiceError>> with the error response.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ServiceError> {
        log.error(VALIDATION_FAILED_LOG, ex)

        val errors =
            ex.bindingResult.allErrors.map { error ->
                ServiceError.DetailedErrorMessage(
                    resource = (error as? FieldError)?.objectName,
                    field = (error as? FieldError)?.field,
                    reason = error.defaultMessage ?: VALIDATION_FAILED_MESSAGE,
                )
            }

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_VALIDATION,
                message = VALIDATION_FAILED_MESSAGE,
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
                errorMessages = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serviceError)
    }

    /**
     * Handles constraint violations from @Validated annotation.
     *
     * @param ex The constraint violation exception to handle.
     * @return Mono<ResponseEntity<ServiceError>> with the error response.
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ServiceError> {
        log.error(CONSTRAINT_VIOLATION_LOG, ex)

        val errors =
            ex.constraintViolations.map { violation ->
                ServiceError.DetailedErrorMessage(
                    field = getFieldName(violation),
                    reason = violation.message,
                )
            }

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_VALIDATION,
                message = VALIDATION_FAILED_MESSAGE,
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
                errorMessages = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serviceError)
    }

    /**
     * Handles unsupported media type errors.
     *
     * @param ex The media type exception to handle.
     * @return Mono<ResponseEntity<ServiceError>> with the error response.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ServiceError> {
        log.error(UNSUPPORTED_MEDIA_TYPE_LOG, ex)

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_UNSUPPORTED_MEDIA_TYPE,
                message = "$UNSUPPORTED_MEDIA_TYPE_MESSAGE${ex.contentType}",
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
            )

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(serviceError)
    }

    /**
     * Handles unsupported HTTP method errors.
     *
     * @param ex The HTTP method exception to handle.
     * @return Mono<ResponseEntity<ServiceError>> with the error response.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupported(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ServiceError> {
        log.error(METHOD_NOT_SUPPORTED_LOG, ex)

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_METHOD_NOT_ALLOWED,
                message = "$METHOD_NOT_SUPPORTED_MESSAGE${ex.method}",
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
            )

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(serviceError)
    }

    /**
     * Handles malformed request body errors.
     *
     * @param ex The message not readable exception to handle.
     * @return Mono<ResponseEntity<ServiceError>> with the error response.
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ServiceError> {
        log.error(MESSAGE_NOT_READABLE_LOG, ex)

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_BAD_REQUEST,
                message = MALFORMED_REQUEST_BODY,
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serviceError)
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex The unexpected exception to handle.
     * @return Mono<ResponseEntity<ServiceError>> with the error response.
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ServiceError> {
        log.error(UNEXPECTED_ERROR_LOG, ex)

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_INTERNAL,
                message = UNEXPECTED_ERROR_MESSAGE,
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
            )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceError)
    }

    /**
     * Extracts the field name from a constraint violation path.
     *
     * @param violation The constraint violation to extract the field name from.
     * @return The extracted field name.
     */
    private fun getFieldName(violation: ConstraintViolation<*>): String {
        val propertyPath = violation.propertyPath.toString()
        val parts = propertyPath.split(".")
        return if (parts.isNotEmpty()) parts.last() else propertyPath
    }

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

        private const val DOMAIN_EXCEPTION_LOG = "Domain exception: {} - {}"
        private const val VALIDATION_FAILED_LOG = "Validation failed"
        private const val VALIDATION_FAILED_MESSAGE = "Validation failed"
        private const val CONSTRAINT_VIOLATION_LOG = "Constraint violation"
        private const val UNSUPPORTED_MEDIA_TYPE_LOG = "Unsupported media type"
        private const val UNSUPPORTED_MEDIA_TYPE_MESSAGE = "Unsupported media type: "
        private const val METHOD_NOT_SUPPORTED_LOG = "Method not supported"
        private const val METHOD_NOT_SUPPORTED_MESSAGE = "HTTP method not supported: "
        private const val MESSAGE_NOT_READABLE_LOG = "Message not readable"
        private const val MALFORMED_REQUEST_BODY = "Malformed request body"
        private const val UNEXPECTED_ERROR_LOG = "Unexpected error"
        private const val UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred"

        private const val ERROR_CODE_VALIDATION = 40001
        private const val ERROR_CODE_METHOD_NOT_ALLOWED = 40501
        private const val ERROR_CODE_UNSUPPORTED_MEDIA_TYPE = 41501
        private const val ERROR_CODE_BAD_REQUEST = 40002
        private const val ERROR_CODE_INTERNAL = 50001
    }
}
