package io.github.balaelangovan.spring.webflux.starter.security

/**
 * Method-level annotation for declarative authorization in reactive WebFlux applications.
 * Checks if a user belongs to any of the authorized groups by examining request headers.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Authorization(
    /**
     * List of authorized groups. User must belong to at least one of these groups.
     */
    val authorizedGroups: Array<String> = [],
    /**
     * Header names to check for user groups.
     */
    val headerNames: Array<String> = ["X-User-Groups"],
    /**
     * Delimiter used to separate multiple groups in header value.
     */
    val delimiter: String = ",",
)
