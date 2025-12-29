package io.github.balaelangovan.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Micrometer implementation of [MetricsCollector].
 * Works with both Spring Boot WebMVC and WebFlux applications.
 *
 * @param meterRegistry the Micrometer meter registry
 */
@Component
class MicrometerMetricsCollector(
    private val meterRegistry: MeterRegistry,
) : MetricsCollector {
    override fun incrementCounter(
        name: String,
        vararg tags: String,
    ) {
        try {
            Counter
                .builder(name)
                .tags(*tags)
                .register(meterRegistry)
                .increment()
            log.debug(COUNTER_INCREMENTED, name)
        } catch (e: Exception) {
            log.error(COUNTER_FAILED, name, e)
        }
    }

    override fun recordGauge(
        name: String,
        value: Double,
        vararg tags: String,
    ) {
        try {
            Gauge
                .builder(name) { value }
                .tags(*tags)
                .register(meterRegistry)
            log.debug(GAUGE_RECORDED, name, value)
        } catch (e: Exception) {
            log.error(GAUGE_FAILED, name, e)
        }
    }

    override fun recordTimer(
        name: String,
        durationMs: Long,
        vararg tags: String,
    ) {
        try {
            Timer
                .builder(name)
                .tags(*tags)
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs))
            log.debug(TIMER_RECORDED, name, durationMs)
        } catch (e: Exception) {
            log.error(TIMER_FAILED, name, e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(MicrometerMetricsCollector::class.java)

        private const val COUNTER_INCREMENTED = "Incremented counter: {}"
        private const val COUNTER_FAILED = "Failed to increment counter: {}"
        private const val GAUGE_RECORDED = "Recorded gauge: {} = {}"
        private const val GAUGE_FAILED = "Failed to record gauge: {}"
        private const val TIMER_RECORDED = "Recorded timer: {} = {}ms"
        private const val TIMER_FAILED = "Failed to record timer: {}"
    }
}
