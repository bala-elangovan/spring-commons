package io.github.balaelangovan.security

import io.github.balaelangovan.constants.MdcKeys
import io.github.balaelangovan.exception.ForbiddenException
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * AOP aspect that enforces authorization rules defined by [Authorization] annotation.
 * Checks user groups from request headers against authorized groups.
 */
@Aspect
@Component
class AuthorizationAspect {
    /**
     * Intercepts methods annotated with @Authorization and performs authorization check.
     *
     * @param authorization the Authorization annotation containing authorized groups
     * @throws ForbiddenException if the user is not authorized
     */
    @Before("@annotation(authorization)")
    fun authorize(authorization: Authorization) {
        val authorizedGroups = authorization.authorizedGroups

        if (authorizedGroups.isEmpty()) {
            return
        }

        val request =
            getCurrentRequest()
                ?: run {
                    log.warn(NO_HTTP_REQUEST)
                    throw ForbiddenException(FORBIDDEN_UNABLE_TO_VERIFY)
                }

        val userGroups = extractUserGroups(request, authorization)
        val isAuthorized = authorizedGroups.any { it.trim() in userGroups }

        if (!isAuthorized) {
            val userId = MDC.get(MdcKeys.USER_ID)
            log.warn(
                AUTH_FAILED,
                userId,
                userGroups,
                authorizedGroups.contentToString(),
            )
            throw ForbiddenException(FORBIDDEN_INSUFFICIENT_PERMISSIONS)
        }

        log.debug(AUTH_SUCCESSFUL, MDC.get(MdcKeys.USER_ID))
    }

    /**
     * Gets the current HTTP request from RequestContextHolder.
     *
     * @return the current HTTP request, or null if not in request context
     */
    private fun getCurrentRequest(): HttpServletRequest? = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    /**
     * Extracts user groups from request headers based on annotation configuration.
     *
     * @param request the HTTP request
     * @param authorization the Authorization annotation
     * @return set of user groups
     */
    private fun extractUserGroups(
        request: HttpServletRequest,
        authorization: Authorization,
    ): Set<String> = authorization.headerNames
        .mapNotNull { request.getHeader(it) }
        .filter { StringUtils.isNotBlank(it) }
        .flatMap { it.split(authorization.delimiter) }
        .map { it.trim() }
        .toSet()

    companion object {
        private val log = LoggerFactory.getLogger(AuthorizationAspect::class.java)

        private const val NO_HTTP_REQUEST = "No HTTP request found in context"
        private const val FORBIDDEN_UNABLE_TO_VERIFY = "Access forbidden: Unable to verify authorization"
        private const val AUTH_FAILED = "Authorization failed for user: {}, userGroups: {}, requiredGroups: {}"
        private const val FORBIDDEN_INSUFFICIENT_PERMISSIONS = "Access forbidden: User does not have required permissions"
        private const val AUTH_SUCCESSFUL = "Authorization successful for user: {}"
    }
}
