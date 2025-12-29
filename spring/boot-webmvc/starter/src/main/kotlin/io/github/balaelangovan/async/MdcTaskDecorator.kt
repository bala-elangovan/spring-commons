package io.github.balaelangovan.async

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator
import org.springframework.stereotype.Component

/**
 * Task decorator that propagates MDC context to async threads.
 * Ensures logging context is preserved when using @Async methods.
 */
@Component
class MdcTaskDecorator : TaskDecorator {
    /**
     * Wraps the given Runnable to copy MDC context from the calling thread to the async thread.
     *
     * @param runnable the original runnable
     * @return decorated runnable with MDC propagation
     */
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()

        return Runnable {
            try {
                contextMap?.let { MDC.setContextMap(it) }
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}
