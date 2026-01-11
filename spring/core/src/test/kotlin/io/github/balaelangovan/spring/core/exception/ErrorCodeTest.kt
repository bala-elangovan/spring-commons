package io.github.balaelangovan.spring.core.exception

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus

class ErrorCodeTest :
    DescribeSpec({

        describe("ErrorCode") {
            it("VALIDATION_FAILED should have BAD_REQUEST status") {
                ErrorCode.VALIDATION_FAILED.httpStatus shouldBe HttpStatus.BAD_REQUEST
                ErrorCode.VALIDATION_FAILED.code shouldBe "VALIDATION_FAILED"
                ErrorCode.VALIDATION_FAILED.defaultMessage shouldBe "Validation failed for the request"
            }

            it("INVALID_REQUEST should have BAD_REQUEST status") {
                ErrorCode.INVALID_REQUEST.httpStatus shouldBe HttpStatus.BAD_REQUEST
                ErrorCode.INVALID_REQUEST.code shouldBe "INVALID_REQUEST"
                ErrorCode.INVALID_REQUEST.defaultMessage shouldBe "Invalid request parameters"
            }

            it("RESOURCE_NOT_FOUND should have NOT_FOUND status") {
                ErrorCode.RESOURCE_NOT_FOUND.httpStatus shouldBe HttpStatus.NOT_FOUND
                ErrorCode.RESOURCE_NOT_FOUND.code shouldBe "RESOURCE_NOT_FOUND"
                ErrorCode.RESOURCE_NOT_FOUND.defaultMessage shouldBe "Requested resource not found"
            }

            it("UNAUTHORIZED should have UNAUTHORIZED status") {
                ErrorCode.UNAUTHORIZED.httpStatus shouldBe HttpStatus.UNAUTHORIZED
                ErrorCode.UNAUTHORIZED.code shouldBe "UNAUTHORIZED"
                ErrorCode.UNAUTHORIZED.defaultMessage shouldBe "Authentication required"
            }

            it("FORBIDDEN should have FORBIDDEN status") {
                ErrorCode.FORBIDDEN.httpStatus shouldBe HttpStatus.FORBIDDEN
                ErrorCode.FORBIDDEN.code shouldBe "FORBIDDEN"
                ErrorCode.FORBIDDEN.defaultMessage shouldBe "Access forbidden"
            }

            it("CONFLICT should have CONFLICT status") {
                ErrorCode.CONFLICT.httpStatus shouldBe HttpStatus.CONFLICT
                ErrorCode.CONFLICT.code shouldBe "CONFLICT"
                ErrorCode.CONFLICT.defaultMessage shouldBe "Resource conflict detected"
            }

            it("INTERNAL_ERROR should have INTERNAL_SERVER_ERROR status") {
                ErrorCode.INTERNAL_ERROR.httpStatus shouldBe HttpStatus.INTERNAL_SERVER_ERROR
                ErrorCode.INTERNAL_ERROR.code shouldBe "INTERNAL_ERROR"
                ErrorCode.INTERNAL_ERROR.defaultMessage shouldBe "Internal server error occurred"
            }

            it("SERVICE_UNAVAILABLE should have SERVICE_UNAVAILABLE status") {
                ErrorCode.SERVICE_UNAVAILABLE.httpStatus shouldBe HttpStatus.SERVICE_UNAVAILABLE
                ErrorCode.SERVICE_UNAVAILABLE.code shouldBe "SERVICE_UNAVAILABLE"
                ErrorCode.SERVICE_UNAVAILABLE.defaultMessage shouldBe "Service temporarily unavailable"
            }

            it("should have 8 error codes") {
                ErrorCode.entries.size shouldBe 8
            }
        }
    })
