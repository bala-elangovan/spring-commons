package io.github.balaelangovan.spring.webmvc.starter.async

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

/**
 * Task decorator that propagates MDC context to async threads.
 * Ensures logging context is preserved when using @Async methods.
 */
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
