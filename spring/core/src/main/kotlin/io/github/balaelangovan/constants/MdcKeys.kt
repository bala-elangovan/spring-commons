package io.github.balaelangovan.constants

/**
 * MDC (Mapped Diagnostic Context) key constants for structured logging.
 * These keys are used to add contextual information to log entries.
 */
object MdcKeys {
    const val USER_ID = "user_id"
    const val USER_EMAIL = "user_email"
    const val USER_GROUPS = "user_groups"

    const val TRANSACTION_ID = "transaction_id"
    const val CLIENT_TRANSACTION_ID = "client_transaction_id"
    const val CLIENT_ID = "client_id"

    const val REQUEST_METHOD = "request_method"
    const val REQUEST_URL = "request_url"
    const val REQUEST_DURATION_MS = "request_duration_ms"
    const val CLIENT_IP = "client_ip"

    const val RESPONSE_STATUS = "response_status"

    const val SERVICE_NAME = "service_name"
    const val SERVICE_VERSION = "service_version"
}
