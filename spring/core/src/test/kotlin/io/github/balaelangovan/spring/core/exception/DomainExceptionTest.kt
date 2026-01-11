package io.github.balaelangovan.spring.core.exception

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class DomainExceptionTest :
    DescribeSpec({

        describe("ValidationException") {
            it("should create with message only") {
                val exception = ValidationException("Invalid input")

                exception.message shouldBe "Invalid input"
                exception.errorCode shouldBe ErrorCode.VALIDATION_FAILED
                exception.resource shouldBe null
                exception.field shouldBe null
                exception.cause shouldBe null
            }

            it("should create with message and cause") {
                val cause = RuntimeException("root cause")
                val exception = ValidationException("Invalid input", cause)

                exception.message shouldBe "Invalid input"
                exception.cause shouldBe cause
            }

            it("should create with resource, field, and message") {
                val exception = ValidationException("User", "email", "Invalid email format")

                exception.message shouldBe "Invalid email format"
                exception.resource shouldBe "User"
                exception.field shouldBe "email"
            }

            it("should be a DomainException") {
                val exception = ValidationException("test")
                exception.shouldBeInstanceOf<DomainException>()
            }

            it("should be a RuntimeException") {
                val exception = ValidationException("test")
                exception.shouldBeInstanceOf<RuntimeException>()
            }
        }

        describe("ResourceNotFoundException") {
            it("should create with message only") {
                val exception = ResourceNotFoundException("User not found")

                exception.message shouldBe "User not found"
                exception.errorCode shouldBe ErrorCode.RESOURCE_NOT_FOUND
                exception.resource shouldBe null
                exception.field shouldBe null
            }

            it("should create with message and cause") {
                val cause = RuntimeException("db error")
                val exception = ResourceNotFoundException("User not found", cause)

                exception.message shouldBe "User not found"
                exception.cause shouldBe cause
            }

            it("should create with resource, field, and message") {
                val exception = ResourceNotFoundException("User", "id", "User with id 123 not found")

                exception.resource shouldBe "User"
                exception.field shouldBe "id"
                exception.message shouldBe "User with id 123 not found"
            }
        }

        describe("ConflictException") {
            it("should create with message only") {
                val exception = ConflictException("Duplicate entry")

                exception.message shouldBe "Duplicate entry"
                exception.errorCode shouldBe ErrorCode.CONFLICT
            }

            it("should create with message and cause") {
                val cause = RuntimeException("constraint violation")
                val exception = ConflictException("Duplicate entry", cause)

                exception.message shouldBe "Duplicate entry"
                exception.cause shouldBe cause
            }

            it("should create with resource, field, and message") {
                val exception = ConflictException("User", "email", "Email already exists")

                exception.resource shouldBe "User"
                exception.field shouldBe "email"
                exception.message shouldBe "Email already exists"
            }
        }

        describe("ForbiddenException") {
            it("should create with message only") {
                val exception = ForbiddenException("Access denied")

                exception.message shouldBe "Access denied"
                exception.errorCode shouldBe ErrorCode.FORBIDDEN
            }

            it("should create with message and cause") {
                val cause = RuntimeException("permission error")
                val exception = ForbiddenException("Access denied", cause)

                exception.message shouldBe "Access denied"
                exception.cause shouldBe cause
            }

            it("should create with resource, field, and message") {
                val exception = ForbiddenException("AdminPanel", "role", "Requires admin role")

                exception.resource shouldBe "AdminPanel"
                exception.field shouldBe "role"
                exception.message shouldBe "Requires admin role"
            }
        }
    })
