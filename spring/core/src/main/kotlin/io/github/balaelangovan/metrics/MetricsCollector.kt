package io.github.balaelangovan.metrics

/**
 * Interface for collecting application metrics.
 * Provides abstraction over the underlying metrics implementation.
 */
interface MetricsCollector {
    /**
     * Increments a counter metric.
     *
     * @param name metric name
     * @param tags optional key-value tags
     */
    fun incrementCounter(
        name: String,
        vararg tags: String,
    )

    /**
     * Records a gauge metric value.
     *
     * @param name metric name
     * @param value metric value
     * @param tags optional key-value tags
     */
    fun recordGauge(
        name: String,
        value: Double,
        vararg tags: String,
    )

    /**
     * Records a timer metric duration.
     *
     * @param name metric name
     * @param durationMs duration in milliseconds
     * @param tags optional key-value tags
     */
    fun recordTimer(
        name: String,
        durationMs: Long,
        vararg tags: String,
    )
}
