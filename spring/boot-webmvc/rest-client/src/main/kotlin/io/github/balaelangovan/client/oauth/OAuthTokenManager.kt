package io.github.balaelangovan.client.oauth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant

/**
 * Manages OAuth2 access tokens with automatic refresh and caching.
 * First checks Authorization header from request context, then generates token if not present.
 *
 * @param restTemplate the REST template for token requests
 * @param oAuthProperties OAuth configuration properties
 */
@Component
@ConditionalOnProperty(prefix = "platform.rest-client.oauth", name = ["enabled"])
class OAuthTokenManager(
    private val restTemplate: RestTemplate,
    private val oAuthProperties: OAuthProperties,
) {
    @Volatile
    private var cachedToken: CachedToken? = null

    /**
     * Gets OAuth token from Authorization header or generates new one.
     *
     * @return the access token
     */
    @Synchronized
    fun getToken(): String {
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            requestAttributes?.request?.let { request ->
                val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
                if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                    val token = authHeader.substring(7)
                    log.debug(USING_TOKEN_FROM_HEADER)
                    return token
                }
            }
        } catch (e: IllegalStateException) {
            log.debug(NO_REQUEST_CONTEXT)
        }

        if (cachedToken == null || isExpired(cachedToken!!)) {
            log.debug(TOKEN_REFRESHING)
            refreshToken()
        }
        return cachedToken!!.accessToken
    }

    private fun refreshToken() {
        try {
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_FORM_URLENCODED
                    setBasicAuth(
                        oAuthProperties.clientId ?: "",
                        oAuthProperties.clientSecret ?: "",
                    )
                }

            val body =
                LinkedMultiValueMap<String, String>().apply {
                    add("grant_type", oAuthProperties.grantType)
                    oAuthProperties.scope?.let { add("scope", it) }
                }

            val request = HttpEntity(body, headers)

            val response =
                restTemplate.postForEntity(
                    requireNotNull(oAuthProperties.tokenUrl) { TOKEN_URL_REQUIRED },
                    request,
                    Map::class.java,
                )

            response.body?.let { responseBody ->
                val accessToken = responseBody["access_token"] as String
                val expiresIn = responseBody["expires_in"] as Int
                val expiresAt = Instant.now().plusSeconds(expiresIn.toLong() - 60)
                cachedToken = CachedToken(accessToken, expiresAt)

                log.info(TOKEN_REFRESHED_SUCCESS, expiresAt)
            }
        } catch (e: Exception) {
            log.error(TOKEN_REFRESH_FAILED, e)
            throw RuntimeException(TOKEN_REFRESH_EXCEPTION, e)
        }
    }

    private fun isExpired(token: CachedToken): Boolean = Instant.now().isAfter(token.expiresAt)

    private data class CachedToken(
        val accessToken: String,
        val expiresAt: Instant,
    )

    companion object {
        private val log = LoggerFactory.getLogger(OAuthTokenManager::class.java)

        private const val BEARER_PREFIX = "Bearer "
        private const val USING_TOKEN_FROM_HEADER = "Using token from Authorization header"
        private const val NO_REQUEST_CONTEXT = "No request context available, will generate token"
        private const val TOKEN_REFRESHING = "Token expired or not available, refreshing..."
        private const val TOKEN_REFRESHED_SUCCESS = "OAuth token refreshed successfully, expires at: {}"
        private const val TOKEN_REFRESH_FAILED = "Failed to refresh OAuth token"
        private const val TOKEN_REFRESH_EXCEPTION = "OAuth token refresh failed"
        private const val TOKEN_URL_REQUIRED = "OAuth token URL must be configured"
    }
}
