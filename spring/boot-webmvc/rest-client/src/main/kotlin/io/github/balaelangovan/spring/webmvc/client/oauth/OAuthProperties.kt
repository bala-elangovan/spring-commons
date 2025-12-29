package io.github.balaelangovan.spring.webmvc.client.oauth

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for OAuth2 client credentials flow.
 *
 * @param enabled whether OAuth is enabled
 * @param tokenUrl the OAuth token endpoint URL
 * @param clientId the OAuth client ID
 * @param clientSecret the OAuth client secret
 * @param grantType the OAuth grant type (default: client_credentials)
 * @param scope optional OAuth scope
 */
@ConfigurationProperties(prefix = "platform.rest-client.oauth")
data class OAuthProperties(
    var enabled: Boolean = false,
    var tokenUrl: String? = null,
    var clientId: String? = null,
    var clientSecret: String? = null,
    var grantType: String = "client_credentials",
    var scope: String? = null,
)
