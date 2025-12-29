package io.github.balaelangovan.spring.core.constants

/**
 * HTTP header constants for platform services.
 * Uses generic header names for cross-platform compatibility.
 */
object HeaderConstants {
    const val X_USER_ID = "X-User-Id"
    const val X_USER_EMAIL = "X-User-Email"
    const val X_USER_GROUPS = "X-User-Groups"

    const val X_TRANSACTION_ID = "X-Transaction-Id"
    const val X_CLIENT_TRANSACTION_ID = "X-Client-Transaction-Id"

    const val X_CLIENT_ID = "X-Client-Id"
    const val X_API_KEY = "X-API-Key"
    const val X_SERVICE_ID = "X-Service-Id"

    const val X_FORWARDED_FOR = "X-Forwarded-For"

    const val AUTHORIZATION = "Authorization"
}
