package io.github.balaelangovan.spring.webflux.starter.exception

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
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.test.StepVerifier

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

                    val result = handler.handleDomainException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.BAD_REQUEST
                            response.body shouldNotBe null
                            response.body!!.message shouldBe "Invalid input"
                            response.body!!.transactionId shouldBe "test-tx-id"
                        }
                        .verifyComplete()
                }

                it("should handle ValidationException with resource and field") {
                    val exception = ValidationException("User", "email", "Invalid email format")

                    val result = handler.handleDomainException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.BAD_REQUEST
                            response.body!!.message shouldBe "Invalid email format"
                            response.body!!.errorMessages!! shouldHaveSize 1
                            response.body!!.errorMessages!![0].resource shouldBe "User"
                            response.body!!.errorMessages!![0].field shouldBe "email"
                        }
                        .verifyComplete()
                }

                it("should handle ResourceNotFoundException") {
                    val exception = ResourceNotFoundException("User not found")

                    val result = handler.handleDomainException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.NOT_FOUND
                            response.body!!.message shouldBe "User not found"
                        }
                        .verifyComplete()
                }

                it("should handle ForbiddenException") {
                    val exception = ForbiddenException("Access denied")

                    val result = handler.handleDomainException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.FORBIDDEN
                            response.body!!.message shouldBe "Access denied"
                        }
                        .verifyComplete()
                }

                it("should handle ConflictException") {
                    val exception = ConflictException("Duplicate entry")

                    val result = handler.handleDomainException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.CONFLICT
                            response.body!!.message shouldBe "Duplicate entry"
                        }
                        .verifyComplete()
                }

                it("should use transaction ID from MDC") {
                    MDC.put(MdcKeys.TRANSACTION_ID, "custom-tx-123")
                    val exception = ValidationException("Error")

                    val result = handler.handleDomainException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.body!!.transactionId shouldBe "custom-tx-123"
                        }
                        .verifyComplete()
                }
            }

            describe("handleValidationException") {
                it("should handle WebExchangeBindException with field errors") {
                    val bindingResult = mockk<BindingResult>()
                    val fieldError = FieldError("userDto", "email", "must not be blank")

                    every { bindingResult.allErrors } returns listOf(fieldError)

                    val exception = WebExchangeBindException(mockk(relaxed = true), bindingResult)

                    val result = handler.handleValidationException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.BAD_REQUEST
                            response.body!!.message shouldBe "Validation failed"
                            response.body!!.errorMessages!! shouldHaveSize 1
                            response.body!!.errorMessages!![0].resource shouldBe "userDto"
                            response.body!!.errorMessages!![0].field shouldBe "email"
                            response.body!!.errorMessages!![0].reason shouldBe "must not be blank"
                        }
                        .verifyComplete()
                }

                it("should handle multiple field errors") {
                    val bindingResult = mockk<BindingResult>()
                    val fieldError1 = FieldError("userDto", "email", "must not be blank")
                    val fieldError2 = FieldError("userDto", "name", "size must be between 1 and 100")

                    every { bindingResult.allErrors } returns listOf(fieldError1, fieldError2)

                    val exception = WebExchangeBindException(mockk(relaxed = true), bindingResult)

                    val result = handler.handleValidationException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.body!!.errorMessages!! shouldHaveSize 2
                        }
                        .verifyComplete()
                }

                it("should use default message when field error has no message") {
                    val bindingResult = mockk<BindingResult>()
                    val objectError = mockk<ObjectError>()
                    every { objectError.defaultMessage } returns null

                    every { bindingResult.allErrors } returns listOf(objectError)

                    val exception = WebExchangeBindException(mockk(relaxed = true), bindingResult)

                    val result = handler.handleValidationException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.body!!.errorMessages!![0].reason shouldBe "Validation failed"
                        }
                        .verifyComplete()
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

                    val result = handler.handleConstraintViolationException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.BAD_REQUEST
                            response.body!!.message shouldBe "Validation failed"
                            response.body!!.errorMessages!! shouldHaveSize 1
                            response.body!!.errorMessages!![0].field shouldBe "email"
                            response.body!!.errorMessages!![0].reason shouldBe "must be a valid email"
                        }
                        .verifyComplete()
                }

                it("should extract last path segment as field name") {
                    val violation = mockk<ConstraintViolation<Any>>()
                    val path = mockk<Path>()

                    every { path.toString() } returns "controller.method.arg.nested.field"
                    every { violation.propertyPath } returns path
                    every { violation.message } returns "error"

                    val exception = ConstraintViolationException(setOf(violation))

                    val result = handler.handleConstraintViolationException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.body!!.errorMessages!![0].field shouldBe "field"
                        }
                        .verifyComplete()
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

                    val result = handler.handleConstraintViolationException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.body!!.errorMessages!! shouldHaveSize 2
                        }
                        .verifyComplete()
                }
            }

            describe("handleGenericException") {
                it("should handle unexpected exceptions") {
                    val exception = RuntimeException("Something went wrong")

                    val result = handler.handleGenericException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                            response.body!!.message shouldBe "An unexpected error occurred"
                            response.body!!.errorCode shouldBe 50001
                            response.body!!.transactionId shouldBe "test-tx-id"
                        }
                        .verifyComplete()
                }

                it("should handle null pointer exceptions") {
                    val exception = NullPointerException("null value")

                    val result = handler.handleGenericException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                            response.body!!.message shouldBe "An unexpected error occurred"
                        }
                        .verifyComplete()
                }

                it("should handle exceptions when MDC has no transaction ID") {
                    MDC.clear()
                    val exception = RuntimeException("Error")

                    val result = handler.handleGenericException(exception)

                    StepVerifier.create(result)
                        .assertNext { response ->
                            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                            response.body!!.transactionId shouldBe null
                        }
                        .verifyComplete()
                }
            }
        }
    })
