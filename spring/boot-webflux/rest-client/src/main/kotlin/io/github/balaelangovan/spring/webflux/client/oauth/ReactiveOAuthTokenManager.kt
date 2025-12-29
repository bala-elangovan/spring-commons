package io.github.balaelangovan.spring.webflux.client.oauth

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/**
 * Reactive OAuth2 token manager for WebFlux applications.
 * Manages access tokens with automatic refresh and caching.
 * First checks the Authorization header from the request context, then generates a token if not present.
 */
@Component
@ConditionalOnProperty(prefix = "platform.rest-client.oauth", name = ["enabled"])
class ReactiveOAuthTokenManager(
    private val webClient: WebClient,
    private val oAuthProperties: OAuthProperties,
) {
    private val cachedToken = AtomicReference<CachedToken?>()

    /**
     * Gets OAuth token from Authorization header or generates new one.
     * Checks request context first for the existing token.
     */
    fun getToken(exchange: ServerWebExchange? = null): Mono<String> {
        // First, try to get token from Authorization header
        exchange?.let {
            val authHeader = it.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                val token = authHeader.substring(7)
                log.debug(USING_TOKEN_FROM_HEADER)
                return Mono.just(token)
            }
        }

        // If not in the header, get or refresh the cached token
        return Mono.defer {
            val cached = cachedToken.get()
            if (cached == null || isExpired(cached)) {
                log.debug(TOKEN_REFRESHING)
                refreshToken()
            } else {
                Mono.just(cached.accessToken)
            }
        }
    }

    /**
     * Refreshes OAuth token from the authorization server.
     */
    private fun refreshToken(): Mono<String> {
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

        return webClient
            .post()
            .uri(requireNotNull(oAuthProperties.tokenUrl) { TOKEN_URL_REQUIRED })
            .headers { it.addAll(headers) }
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono(Map::class.java)
            .map { responseBody ->
                val accessToken = responseBody["access_token"] as String
                val expiresIn = responseBody["expires_in"] as Int

                // Cache with a 60-second buffer to prevent using expired tokens
                val expiresAt = Instant.now().plusSeconds(expiresIn.toLong() - 60)
                cachedToken.set(CachedToken(accessToken, expiresAt))

                log.info(TOKEN_REFRESHED_SUCCESS, expiresAt)
                accessToken
            }.doOnError { error ->
                log.error(TOKEN_REFRESH_FAILED, error)
            }
    }

    /**
     * Checks if the cached token is expired.
     */
    private fun isExpired(token: CachedToken): Boolean = Instant.now().isAfter(token.expiresAt)

    /**
     * Cached token with expiry information.
     */
    private data class CachedToken(
        val accessToken: String,
        val expiresAt: Instant,
    )

    companion object {
        private val log = LoggerFactory.getLogger(ReactiveOAuthTokenManager::class.java)

        private const val BEARER_PREFIX = "Bearer "
        private const val USING_TOKEN_FROM_HEADER = "Using token from Authorization header"
        private const val TOKEN_REFRESHING = "Token expired or not available, refreshing..."
        private const val TOKEN_REFRESHED_SUCCESS = "OAuth token refreshed successfully, expires at: {}"
        private const val TOKEN_REFRESH_FAILED = "Failed to refresh OAuth token"
        private const val TOKEN_URL_REQUIRED = "OAuth token URL must be configured"
    }
}
