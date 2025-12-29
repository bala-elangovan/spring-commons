package io.github.balaelangovan.security

import io.github.balaelangovan.constants.MdcKeys
import io.github.balaelangovan.exception.ForbiddenException
import org.apache.commons.lang3.StringUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * AOP aspect that enforces authorization rules defined by @Authorization annotation for reactive WebFlux applications.
 * Checks user groups from request headers against authorized groups.
 */
@Aspect
@Component
class AuthorizationAspect {
    /**
     * Intercepts methods annotated with @Authorization and performs authorization check in reactive context.
     *
     * @param joinPoint The join point representing the intercepted method.
     * @param authorization The @Authorization annotation.
     * @return The result of the intercepted method.
     * @throws ForbiddenException if the user is not authorized
     */
    @Around("@annotation(authorization)")
    fun authorize(
        joinPoint: ProceedingJoinPoint,
        authorization: Authorization,
    ): Any? {
        val authorizedGroups = authorization.authorizedGroups

        // If no authorized groups specified, allow access
        if (authorizedGroups.isEmpty()) {
            return joinPoint.proceed()
        }

        // Try to find ServerWebExchange in method arguments
        val exchange = findServerWebExchange(joinPoint)

        // If exchange is found in arguments, perform an authorization check immediately
        if (exchange != null) {
            performAuthorizationCheck(exchange, authorization, authorizedGroups)
            return joinPoint.proceed()
        }

        // If no exchange found, wrap the result in Mono/Flux and perform check from reactive context
        val result = joinPoint.proceed()

        // Handle Mono return type
        if (result is Mono<*>) {
            return result.transformDeferredContextual { mono, ctx ->
                createAuthorizationCheck(ctx, authorization, authorizedGroups).then(mono)
            }
        }

        // Handle Flux return type
        if (result is Flux<*>) {
            return result.transformDeferredContextual { flux, ctx ->
                createAuthorizationCheck(ctx, authorization, authorizedGroups).thenMany(flux)
            }
        }

        // For non-reactive return types, we cannot perform authorization
        log.warn(NON_REACTIVE_METHOD_WARNING)
        return result
    }

    /**
     * Finds ServerWebExchange in method arguments.
     *
     * @param joinPoint The join point.
     * @return The ServerWebExchange if found, null otherwise.
     */
    private fun findServerWebExchange(joinPoint: ProceedingJoinPoint): ServerWebExchange? = joinPoint.args.firstOrNull { it is ServerWebExchange } as? ServerWebExchange

    /**
     * Creates a Mono that performs authorization check from reactive context.
     *
     * @param ctx The reactive context view.
     * @param authorization The @Authorization annotation.
     * @param authorizedGroups The authorized groups.
     * @return A Mono that completes after authorization check.
     * @throws ForbiddenException if the user is not authorized
     */
    private fun createAuthorizationCheck(
        ctx: reactor.util.context.ContextView,
        authorization: Authorization,
        authorizedGroups: Array<String>,
    ): Mono<Void> = Mono.fromRunnable {
        val contextExchange =
            ctx.getOrDefault(ServerWebExchange::class.java, null)
                ?: run {
                    log.warn(NO_WEB_EXCHANGE)
                    throw ForbiddenException(FORBIDDEN_UNABLE_TO_VERIFY)
                }
        performAuthorizationCheck(contextExchange, authorization, authorizedGroups)
    }

    /**
     * Performs the authorization check.
     *
     * @param exchange The ServerWebExchange.
     * @param authorization The @Authorization annotation.
     * @param authorizedGroups The authorized groups.
     * @throws ForbiddenException if the user is not authorized
     */
    private fun performAuthorizationCheck(
        exchange: ServerWebExchange,
        authorization: Authorization,
        authorizedGroups: Array<String>,
    ) {
        // Extract user groups from headers
        val userGroups = extractUserGroups(exchange, authorization)

        // Check if a user belongs to any authorized group
        val isAuthorized = authorizedGroups.any { it.trim() in userGroups }

        if (!isAuthorized) {
            val userId = exchange.request.headers.getFirst(MdcKeys.USER_ID) ?: "unknown"
            log.warn(
                AUTH_FAILED,
                userId,
                userGroups,
                authorizedGroups.contentToString(),
            )
            throw ForbiddenException(FORBIDDEN_INSUFFICIENT_PERMISSIONS)
        }

        val userId = exchange.request.headers.getFirst(MdcKeys.USER_ID) ?: "unknown"
        log.debug(AUTH_SUCCESSFUL, userId)
    }

    /**
     * Extracts user groups from request headers in ServerWebExchange.
     *
     * @param exchange The current ServerWebExchange.
     * @param authorization The @Authorization annotation.
     * @return The extracted user groups.
     */
    private fun extractUserGroups(
        exchange: ServerWebExchange,
        authorization: Authorization,
    ): Set<String> = authorization.headerNames
        .mapNotNull { exchange.request.headers.getFirst(it) }
        .filter { StringUtils.isNotBlank(it) }
        .flatMap { it.split(authorization.delimiter) }
        .map { it.trim() }
        .toSet()

    companion object {
        private val log = LoggerFactory.getLogger(AuthorizationAspect::class.java)

        private const val NO_WEB_EXCHANGE = "No ServerWebExchange found in reactive context"
        private const val FORBIDDEN_UNABLE_TO_VERIFY = "Access forbidden: Unable to verify authorization"
        private const val AUTH_FAILED = "Authorization failed for user: {}, userGroups: {}, requiredGroups: {}"
        private const val FORBIDDEN_INSUFFICIENT_PERMISSIONS = "Access forbidden: User does not have required permissions"
        private const val AUTH_SUCCESSFUL = "Authorization successful for user: {}"
        private const val NON_REACTIVE_METHOD_WARNING = "Authorization aspect applied to non-reactive method - authorization check skipped"
    }
}
