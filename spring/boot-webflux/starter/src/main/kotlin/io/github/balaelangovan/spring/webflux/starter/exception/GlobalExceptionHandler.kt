package io.github.balaelangovan.spring.webflux.starter.exception

import io.github.balaelangovan.spring.core.constants.MdcKeys
import io.github.balaelangovan.spring.core.dto.ServiceError
import io.github.balaelangovan.spring.core.exception.DomainException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

/**
 * Global exception handler for WebFlux applications.
 * Converts exceptions to standardized [ServiceError] responses.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * Handles domain exceptions with structured error information.
     *
     * @param ex the domain exception
     * @return Mono emitting the error response
     */
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException): Mono<ResponseEntity<ServiceError>> {
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

        return Mono.just(ResponseEntity.status(errorCode.httpStatus).body(serviceError))
    }

    /**
     * Handles validation errors from @Valid annotation.
     *
     * @param ex the validation exception
     * @return Mono emitting the error response
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationException(ex: WebExchangeBindException): Mono<ResponseEntity<ServiceError>> {
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

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serviceError))
    }

    /**
     * Handles constraint violations from @Validated annotation.
     *
     * @param ex the constraint violation exception
     * @return Mono emitting the error response
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): Mono<ResponseEntity<ServiceError>> {
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

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(serviceError))
    }

    /**
     * Handles all other unexpected exceptions.
     *
     * @param ex the exception
     * @return Mono emitting the error response
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): Mono<ResponseEntity<ServiceError>> {
        log.error(UNEXPECTED_ERROR_LOG, ex)

        val serviceError =
            ServiceError(
                errorCode = ERROR_CODE_INTERNAL,
                message = UNEXPECTED_ERROR_MESSAGE,
                transactionId = MDC.get(MdcKeys.TRANSACTION_ID),
            )

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(serviceError))
    }

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
        private const val UNEXPECTED_ERROR_LOG = "Unexpected error"
        private const val UNEXPECTED_ERROR_MESSAGE = "An unexpected error occurred"

        private const val ERROR_CODE_VALIDATION = 40001
        private const val ERROR_CODE_INTERNAL = 50001
    }
}
