package io.github.balaelangovan.spring.webmvc.starter.exception

import io.github.balaelangovan.spring.core.constants.MdcKeys
import io.github.balaelangovan.spring.core.exception.ConflictException
import io.github.balaelangovan.spring.core.exception.ForbiddenException
import io.github.balaelangovan.spring.core.exception.ResourceNotFoundException
import io.github.balaelangovan.spring.core.exception.ValidationException
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTest :
    DescribeSpec({

        val handler = GlobalExceptionHandler()

        beforeEach {
            MDC.put(MdcKeys.TRANSACTION_ID, "test-tx-id")
        }

        afterEach {
            MDC.clear()
        }

        describe("GlobalExceptionHandler") {
            describe("handleDomainException") {
                it("should handle ValidationException with message only") {
                    val exception = ValidationException("Invalid input")

                    val response = handler.handleDomainException(exception)

                    response.statusCode shouldBe HttpStatus.BAD_REQUEST
                    response.body shouldNotBe null
                    response.body!!.message shouldBe "Invalid input"
                    response.body!!.transactionId shouldBe "test-tx-id"
                }

                it("should handle ValidationException with resource and field") {
                    val exception = ValidationException("User", "email", "Invalid email format")

                    val response = handler.handleDomainException(exception)

                    response.statusCode shouldBe HttpStatus.BAD_REQUEST
                    response.body!!.message shouldBe "Invalid email format"
                    response.body!!.errorMessages!! shouldHaveSize 1
                    response.body!!.errorMessages!![0].resource shouldBe "User"
                    response.body!!.errorMessages!![0].field shouldBe "email"
                    response.body!!.errorMessages!![0].reason shouldBe "Invalid email format"
                }

                it("should handle ResourceNotFoundException") {
                    val exception = ResourceNotFoundException("User not found")

                    val response = handler.handleDomainException(exception)

                    response.statusCode shouldBe HttpStatus.NOT_FOUND
                    response.body!!.message shouldBe "User not found"
                }

                it("should handle ResourceNotFoundException with resource and field") {
                    val exception = ResourceNotFoundException("User", "id", "User with id 123 not found")

                    val response = handler.handleDomainException(exception)

                    response.statusCode shouldBe HttpStatus.NOT_FOUND
                    response.body!!.errorMessages!! shouldHaveSize 1
                    response.body!!.errorMessages!![0].resource shouldBe "User"
                    response.body!!.errorMessages!![0].field shouldBe "id"
                }

                it("should handle ForbiddenException") {
                    val exception = ForbiddenException("Access denied")

                    val response = handler.handleDomainException(exception)

                    response.statusCode shouldBe HttpStatus.FORBIDDEN
                    response.body!!.message shouldBe "Access denied"
                }

                it("should handle ConflictException") {
                    val exception = ConflictException("Duplicate entry")

                    val response = handler.handleDomainException(exception)

                    response.statusCode shouldBe HttpStatus.CONFLICT
                    response.body!!.message shouldBe "Duplicate entry"
                }

                it("should use transaction ID from MDC") {
                    MDC.put(MdcKeys.TRANSACTION_ID, "custom-tx-123")
                    val exception = ValidationException("Error")

                    val response = handler.handleDomainException(exception)

                    response.body!!.transactionId shouldBe "custom-tx-123"
                }
            }

            describe("handleValidationException") {
                it("should handle MethodArgumentNotValidException with field errors") {
                    val bindingResult = mockk<BindingResult>()
                    val fieldError = FieldError("userDto", "email", "must not be blank")

                    every { bindingResult.allErrors } returns listOf(fieldError)

                    val exception = MethodArgumentNotValidException(mockk(relaxed = true), bindingResult)

                    val response = handler.handleValidationException(exception)

                    response.statusCode shouldBe HttpStatus.BAD_REQUEST
                    response.body!!.message shouldBe "Validation failed"
                    response.body!!.errorMessages!! shouldHaveSize 1
                    response.body!!.errorMessages!![0].resource shouldBe "userDto"
                    response.body!!.errorMessages!![0].field shouldBe "email"
                    response.body!!.errorMessages!![0].reason shouldBe "must not be blank"
                }

                it("should handle multiple field errors") {
                    val bindingResult = mockk<BindingResult>()
                    val fieldError1 = FieldError("userDto", "email", "must not be blank")
                    val fieldError2 = FieldError("userDto", "name", "size must be between 1 and 100")

                    every { bindingResult.allErrors } returns listOf(fieldError1, fieldError2)

                    val exception = MethodArgumentNotValidException(mockk(relaxed = true), bindingResult)

                    val response = handler.handleValidationException(exception)

                    response.body!!.errorMessages!! shouldHaveSize 2
                }

                it("should use default message when field error has no message") {
                    val bindingResult = mockk<BindingResult>()
                    // Use ObjectError instead of FieldError to avoid the non-null constraint on defaultMessage
                    val objectError = mockk<ObjectError>()
                    every { objectError.defaultMessage } returns null

                    every { bindingResult.allErrors } returns listOf(objectError)

                    val exception = MethodArgumentNotValidException(mockk(relaxed = true), bindingResult)

                    val response = handler.handleValidationException(exception)

                    response.body!!.errorMessages!![0].reason shouldBe "Validation failed"
                }
            }

            describe("handleConstraintViolationException") {
                it("should handle constraint violations") {
                    val violation = mockk<ConstraintViolation<Any>>()
                    val path = mockk<Path>()

                    every { path.toString() } returns "createUser.email"
                    every { violation.propertyPath } returns path
                    every { violation.message } returns "must be a valid email"

                    val exception = ConstraintViolationException(setOf(violation))

                    val response = handler.handleConstraintViolationException(exception)

                    response.statusCode shouldBe HttpStatus.BAD_REQUEST
                    response.body!!.message shouldBe "Validation failed"
                    response.body!!.errorMessages!! shouldHaveSize 1
                    response.body!!.errorMessages!![0].field shouldBe "email"
                    response.body!!.errorMessages!![0].reason shouldBe "must be a valid email"
                }

                it("should extract last path segment as field name") {
                    val violation = mockk<ConstraintViolation<Any>>()
                    val path = mockk<Path>()

                    every { path.toString() } returns "controller.method.arg.nested.field"
                    every { violation.propertyPath } returns path
                    every { violation.message } returns "error"

                    val exception = ConstraintViolationException(setOf(violation))

                    val response = handler.handleConstraintViolationException(exception)

                    response.body!!.errorMessages!![0].field shouldBe "field"
                }

                it("should handle multiple violations") {
                    val violation1 = mockk<ConstraintViolation<Any>>()
                    val violation2 = mockk<ConstraintViolation<Any>>()
                    val path1 = mockk<Path>()
                    val path2 = mockk<Path>()

                    every { path1.toString() } returns "email"
                    every { violation1.propertyPath } returns path1
                    every { violation1.message } returns "invalid email"

                    every { path2.toString() } returns "name"
                    every { violation2.propertyPath } returns path2
                    every { violation2.message } returns "too short"

                    val exception = ConstraintViolationException(setOf(violation1, violation2))

                    val response = handler.handleConstraintViolationException(exception)

                    response.body!!.errorMessages!! shouldHaveSize 2
                }
            }

            describe("handleHttpMediaTypeNotSupported") {
                it("should handle unsupported media type") {
                    val exception = HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, listOf(MediaType.APPLICATION_JSON))

                    val response = handler.handleHttpMediaTypeNotSupported(exception)

                    response.statusCode shouldBe HttpStatus.UNSUPPORTED_MEDIA_TYPE
                    response.body!!.message shouldBe "Unsupported media type: text/plain"
                    response.body!!.errorCode shouldBe 41501
                }
            }

            describe("handleHttpRequestMethodNotSupported") {
                it("should handle unsupported HTTP method") {
                    val exception = HttpRequestMethodNotSupportedException("DELETE", listOf("GET", "POST"))

                    val response = handler.handleHttpRequestMethodNotSupported(exception)

                    response.statusCode shouldBe HttpStatus.METHOD_NOT_ALLOWED
                    response.body!!.message shouldBe "HTTP method not supported: DELETE"
                    response.body!!.errorCode shouldBe 40501
                }
            }

            describe("handleHttpMessageNotReadable") {
                it("should handle malformed request body") {
                    val exception = mockk<HttpMessageNotReadableException>()

                    val response = handler.handleHttpMessageNotReadable(exception)

                    response.statusCode shouldBe HttpStatus.BAD_REQUEST
                    response.body!!.message shouldBe "Malformed request body"
                    response.body!!.errorCode shouldBe 40002
                }
            }

            describe("handleGenericException") {
                it("should handle unexpected exceptions") {
                    val exception = RuntimeException("Something went wrong")

                    val response = handler.handleGenericException(exception)

                    response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                    response.body!!.message shouldBe "An unexpected error occurred"
                    response.body!!.errorCode shouldBe 50001
                    response.body!!.transactionId shouldBe "test-tx-id"
                }

                it("should handle null pointer exceptions") {
                    val exception = NullPointerException("null value")

                    val response = handler.handleGenericException(exception)

                    response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                    response.body!!.message shouldBe "An unexpected error occurred"
                }

                it("should handle exceptions when MDC has no transaction ID") {
                    MDC.clear()
                    val exception = RuntimeException("Error")

                    val response = handler.handleGenericException(exception)

                    response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                    response.body!!.transactionId shouldBe null
                }
            }
        }
    })
