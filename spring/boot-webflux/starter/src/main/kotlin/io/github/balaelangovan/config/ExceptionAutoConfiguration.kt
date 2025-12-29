package io.github.balaelangovan.config

import io.github.balaelangovan.exception.GlobalExceptionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Autoconfiguration for WebFlux global exception handling.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(
    prefix = "platform.exception-handling",
    name = ["enabled"],
    matchIfMissing = true,
)
class ExceptionAutoConfiguration {
    /**
     * Provides a GlobalExceptionHandler bean for standardized error responses.
     *
     * @return GlobalExceptionHandler bean.
     */
    @Bean
    fun globalExceptionHandler(): GlobalExceptionHandler = GlobalExceptionHandler()
}
