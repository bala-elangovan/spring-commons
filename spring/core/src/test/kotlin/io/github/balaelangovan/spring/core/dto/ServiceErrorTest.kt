package io.github.balaelangovan.spring.core.dto

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue

class ServiceErrorTest :
    DescribeSpec({

        val objectMapper = JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .build()

        describe("ServiceError") {
            it("should create with required fields") {
                val error = ServiceError(
                    errorCode = 40001,
                    message = "Validation failed",
                )

                error.errorCode shouldBe 40001
                error.message shouldBe "Validation failed"
                error.transactionId shouldBe null
                error.timestamp shouldNotBe null
                error.errorMessages shouldBe null
            }

            it("should create with all fields") {
                val detailedError = ServiceError.DetailedErrorMessage(
                    resource = "User",
                    field = "email",
                    reason = "Invalid email format",
                )
                val error = ServiceError(
                    errorCode = 40001,
                    message = "Validation failed",
                    transactionId = "tx-123",
                    errorMessages = listOf(detailedError),
                )

                error.transactionId shouldBe "tx-123"
                error.errorMessages?.shouldHaveSize(1)
                error.errorMessages?.get(0)?.resource shouldBe "User"
                error.errorMessages?.get(0)?.field shouldBe "email"
                error.errorMessages?.get(0)?.reason shouldBe "Invalid email format"
            }

            it("should serialize to JSON with snake_case property names") {
                val error = ServiceError(
                    errorCode = 40001,
                    message = "Validation failed",
                    transactionId = "tx-123",
                )

                val json = objectMapper.writeValueAsString(error)

                json.contains("error_code") shouldBe true
                json.contains("transaction_id") shouldBe true
            }

            it("should deserialize from JSON") {
                val json = """
                {
                    "error_code": 40001,
                    "message": "Validation failed",
                    "transaction_id": "tx-123"
                }
                """.trimIndent()

                val error = objectMapper.readValue<ServiceError>(json)

                error.errorCode shouldBe 40001
                error.message shouldBe "Validation failed"
                error.transactionId shouldBe "tx-123"
            }

            it("should exclude null fields from JSON") {
                val error = ServiceError(
                    errorCode = 40001,
                    message = "Error",
                )

                val json = objectMapper.writeValueAsString(error)

                json.contains("transaction_id") shouldBe false
                json.contains("error_messages") shouldBe false
            }
        }

        describe("DetailedErrorMessage") {
            it("should create with reason only") {
                val message = ServiceError.DetailedErrorMessage(reason = "Something went wrong")

                message.resource shouldBe null
                message.field shouldBe null
                message.reason shouldBe "Something went wrong"
            }

            it("should create with all fields") {
                val message = ServiceError.DetailedErrorMessage(
                    resource = "Order",
                    field = "quantity",
                    reason = "Must be positive",
                )

                message.resource shouldBe "Order"
                message.field shouldBe "quantity"
                message.reason shouldBe "Must be positive"
            }

            it("should serialize to JSON correctly") {
                val message = ServiceError.DetailedErrorMessage(
                    resource = "User",
                    field = "name",
                    reason = "Cannot be empty",
                )

                val json = objectMapper.writeValueAsString(message)

                json.contains("resource") shouldBe true
                json.contains("field") shouldBe true
                json.contains("reason") shouldBe true
            }
        }
    })
