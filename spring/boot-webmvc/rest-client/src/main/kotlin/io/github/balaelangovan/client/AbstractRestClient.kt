package io.github.balaelangovan.client

import io.github.balaelangovan.client.oauth.OAuthTokenManager
import io.github.balaelangovan.constants.HeaderConstants
import io.github.balaelangovan.constants.MdcKeys
import io.github.balaelangovan.exception.ResourceNotFoundException
import io.github.balaelangovan.exception.ValidationException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

/**
 * Abstract base class for REST clients providing common HTTP operations
 * with OAuth token management and header propagation.
 *
 * @param restTemplate the REST template for HTTP requests
 * @param oAuthTokenManager optional OAuth token manager for authentication
 */
abstract class AbstractRestClient(
    protected val restTemplate: RestTemplate,
    protected val oAuthTokenManager: OAuthTokenManager? = null,
) {
    /**
     * Returns the base URL for this client.
     *
     * @return the base URL
     */
    protected abstract fun getBaseUrl(): String

    /**
     * Performs HTTP GET request.
     *
     * @param T the response type
     * @param path the relative path
     * @param responseType the response class
     * @return the response entity
     */
    protected fun <T : Any> get(
        path: String,
        responseType: Class<T>,
    ): ResponseEntity<T> = exchange(path, HttpMethod.GET, null, responseType)

    /**
     * Performs HTTP POST request.
     *
     * @param T the response type
     * @param path the relative path
     * @param body the request body
     * @param responseType the response class
     * @return the response entity
     */
    protected fun <T : Any> post(
        path: String,
        body: Any?,
        responseType: Class<T>,
    ): ResponseEntity<T> = exchange(path, HttpMethod.POST, body, responseType)

    /**
     * Performs HTTP PUT request.
     *
     * @param T the response type
     * @param path the relative path
     * @param body the request body
     * @param responseType the response class
     * @return the response entity
     */
    protected fun <T : Any> put(
        path: String,
        body: Any?,
        responseType: Class<T>,
    ): ResponseEntity<T> = exchange(path, HttpMethod.PUT, body, responseType)

    /**
     * Performs HTTP DELETE request.
     *
     * @param T the response type
     * @param path the relative path
     * @param responseType the response class
     * @return the response entity
     */
    protected fun <T : Any> delete(
        path: String,
        responseType: Class<T>,
    ): ResponseEntity<T> = exchange(path, HttpMethod.DELETE, null, responseType)

    private fun <T : Any> exchange(
        path: String,
        method: HttpMethod,
        body: Any?,
        responseType: Class<T>,
    ): ResponseEntity<T> {
        val url = getBaseUrl() + path

        return try {
            val headers = buildHeaders()
            val requestEntity = HttpEntity(body, headers)

            log.debug(EXECUTING_REQUEST, method, url)
            val response = restTemplate.exchange(url, method, requestEntity, responseType)
            log.debug(REQUEST_COMPLETED, response.statusCode)

            response
        } catch (e: HttpClientErrorException.NotFound) {
            log.error(RESOURCE_NOT_FOUND_LOG, url, e)
            throw ResourceNotFoundException("$RESOURCE_NOT_FOUND_MESSAGE$url")
        } catch (e: HttpClientErrorException.BadRequest) {
            log.error(BAD_REQUEST_LOG, url, e)
            throw ValidationException("$INVALID_REQUEST_MESSAGE${e.message}")
        } catch (e: HttpClientErrorException) {
            log.error(HTTP_CLIENT_ERROR_LOG, url, e)
            throw RuntimeException("$HTTP_REQUEST_FAILED_MESSAGE${e.message}", e)
        }
    }

    private fun buildHeaders(): HttpHeaders = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON

        oAuthTokenManager?.let {
            set(HeaderConstants.AUTHORIZATION, "$BEARER_PREFIX${it.getToken()}")
        }

        MDC.get(MdcKeys.TRANSACTION_ID)?.let {
            set(HeaderConstants.X_TRANSACTION_ID, it)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AbstractRestClient::class.java)

        private const val EXECUTING_REQUEST = "Executing {} request to {}"
        private const val REQUEST_COMPLETED = "Request completed with status: {}"
        private const val RESOURCE_NOT_FOUND_LOG = "Resource not found: {}"
        private const val RESOURCE_NOT_FOUND_MESSAGE = "Resource not found: "
        private const val BAD_REQUEST_LOG = "Bad request: {}"
        private const val INVALID_REQUEST_MESSAGE = "Invalid request: "
        private const val HTTP_CLIENT_ERROR_LOG = "HTTP client error: {}"
        private const val HTTP_REQUEST_FAILED_MESSAGE = "HTTP request failed: "
        private const val BEARER_PREFIX = "Bearer "
    }
}
