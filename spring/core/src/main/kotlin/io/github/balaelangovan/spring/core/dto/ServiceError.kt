package io.github.balaelangovan.spring.core.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * Standardized error response DTO for all API errors.
 *
 * @param errorCode application-specific error code
 * @param message human-readable error message
 * @param transactionId unique transaction identifier for tracking
 * @param timestamp error occurrence timestamp
 * @param errorMessages detailed field-level error messages
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response structure")
data class ServiceError(
    @field:JsonProperty("error_code")
    @field:Schema(description = "Application-specific error code", example = "40001")
    val errorCode: Int,
    @field:JsonProperty("message")
    @field:Schema(description = "Human-readable error message", example = "Validation failed")
    val message: String,
    @field:JsonProperty("transaction_id")
    @field:Schema(
        description = "Unique transaction identifier for tracking",
        example = "550e8400-e29b-41d4-a716-446655440000",
    )
    val transactionId: String? = null,
    @field:JsonProperty("timestamp")
    @field:Schema(description = "Error occurrence timestamp in ISO-8601 format", example = "2025-01-15T10:30:00Z")
    val timestamp: Instant = Instant.now(),
    @field:JsonProperty("error_messages")
    @field:Schema(description = "Detailed field-level or resource-level error messages")
    val errorMessages: List<DetailedErrorMessage>? = null,
) {
    /**
     * Detailed error information for specific fields or resources.
     *
     * @param resource resource type where error occurred
     * @param field field name that caused the error
     * @param reason specific reason for the error
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed error message for a specific field or resource")
    data class DetailedErrorMessage(
        @field:JsonProperty("resource")
        @field:Schema(description = "Resource type where error occurred", example = "User")
        val resource: String? = null,
        @field:JsonProperty("field")
        @field:Schema(description = "Field name that caused the error", example = "email")
        val field: String? = null,
        @field:JsonProperty("reason")
        @field:Schema(description = "Specific reason for the error", example = "Email format is invalid")
        val reason: String,
    )
}
