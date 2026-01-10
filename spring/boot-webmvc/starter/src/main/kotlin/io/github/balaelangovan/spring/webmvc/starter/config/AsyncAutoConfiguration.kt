package io.github.balaelangovan.spring.webmvc.starter.config

import io.github.balaelangovan.spring.webmvc.starter.async.MdcTaskDecorator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Autoconfiguration for async task execution.
 * Configures a thread pool executor with MDC propagation.
 * Only activates for servlet-based web applications.
 */
@Configuration
@EnableAsync
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "modules.async", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AsyncAutoConfiguration {
    @Bean
    fun mdcTaskDecorator(): MdcTaskDecorator = MdcTaskDecorator()

    /**
     * Creates a task executor with MDC context propagation.
     *
     * @param mdcTaskDecorator The MDC task decorator.
     * @return The created task executor.
     */
    @Bean
    fun taskExecutor(mdcTaskDecorator: MdcTaskDecorator): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 5
        maxPoolSize = 10
        queueCapacity = 25
        setThreadNamePrefix("Async-")
        setTaskDecorator(mdcTaskDecorator)
        initialize()
    }
}
