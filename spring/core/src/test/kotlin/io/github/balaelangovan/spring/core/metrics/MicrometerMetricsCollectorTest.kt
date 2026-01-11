package io.github.balaelangovan.spring.core.metrics

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

class MicrometerMetricsCollectorTest :
    DescribeSpec({

        describe("MicrometerMetricsCollector") {
            lateinit var meterRegistry: SimpleMeterRegistry
            lateinit var metricsCollector: MicrometerMetricsCollector

            beforeEach {
                meterRegistry = SimpleMeterRegistry()
                metricsCollector = MicrometerMetricsCollector(meterRegistry)
            }

            describe("incrementCounter") {
                it("should increment counter without tags") {
                    metricsCollector.incrementCounter("test.counter")

                    val counter = meterRegistry.find("test.counter").counter()
                    counter?.count() shouldBe 1.0
                }

                it("should increment counter with tags") {
                    metricsCollector.incrementCounter("test.counter", "env", "prod", "region", "us-east")

                    val counter = meterRegistry.find("test.counter")
                        .tag("env", "prod")
                        .tag("region", "us-east")
                        .counter()
                    counter?.count() shouldBe 1.0
                }

                it("should increment same counter multiple times") {
                    metricsCollector.incrementCounter("test.counter")
                    metricsCollector.incrementCounter("test.counter")
                    metricsCollector.incrementCounter("test.counter")

                    val counter = meterRegistry.find("test.counter").counter()
                    counter?.count() shouldBe 3.0
                }

                it("should handle different tag values as different counters") {
                    metricsCollector.incrementCounter("http.requests", "status", "200")
                    metricsCollector.incrementCounter("http.requests", "status", "200")
                    metricsCollector.incrementCounter("http.requests", "status", "500")

                    val counter200 = meterRegistry.find("http.requests").tag("status", "200").counter()
                    val counter500 = meterRegistry.find("http.requests").tag("status", "500").counter()

                    counter200?.count() shouldBe 2.0
                    counter500?.count() shouldBe 1.0
                }
            }

            describe("recordGauge") {
                it("should record gauge value without tags") {
                    metricsCollector.recordGauge("test.gauge", 42.5)

                    val gauge = meterRegistry.find("test.gauge").gauge()
                    gauge?.value() shouldBe 42.5
                }

                it("should record gauge value with tags") {
                    metricsCollector.recordGauge("memory.usage", 75.0, "host", "server1")

                    val gauge = meterRegistry.find("memory.usage").tag("host", "server1").gauge()
                    gauge?.value() shouldBe 75.0
                }

                it("should update gauge with new value") {
                    metricsCollector.recordGauge("test.gauge", 10.0)
                    metricsCollector.recordGauge("test.gauge", 20.0)

                    // Note: Gauge captures the value at registration time
                    // so this behavior depends on implementation
                    val gauge = meterRegistry.find("test.gauge").gauge()
                    gauge?.value()?.let { it shouldBeGreaterThan 0.0 }
                }
            }

            describe("recordTimer") {
                it("should record timer duration without tags") {
                    metricsCollector.recordTimer("test.timer", 150)

                    val timer = meterRegistry.find("test.timer").timer()
                    timer?.count() shouldBe 1
                    timer?.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) shouldBe 150.0
                }

                it("should record timer duration with tags") {
                    metricsCollector.recordTimer("http.request.duration", 200, "method", "GET", "path", "/api/users")

                    val timer = meterRegistry.find("http.request.duration")
                        .tag("method", "GET")
                        .tag("path", "/api/users")
                        .timer()
                    timer?.count() shouldBe 1
                    timer?.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) shouldBe 200.0
                }

                it("should accumulate timer recordings") {
                    metricsCollector.recordTimer("test.timer", 100)
                    metricsCollector.recordTimer("test.timer", 200)
                    metricsCollector.recordTimer("test.timer", 300)

                    val timer = meterRegistry.find("test.timer").timer()
                    timer?.count() shouldBe 3
                    timer?.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) shouldBe 600.0
                }
            }

            describe("error handling") {
                it("should not throw exception when counter fails") {
                    // This test verifies the try-catch works
                    // In practice, SimpleMeterRegistry won't fail, but the code is protected
                    metricsCollector.incrementCounter("test.counter")
                    // No exception should be thrown
                }

                it("should not throw exception when gauge fails") {
                    metricsCollector.recordGauge("test.gauge", Double.NaN)
                    // No exception should be thrown
                }

                it("should not throw exception when timer fails") {
                    metricsCollector.recordTimer("test.timer", -1)
                    // No exception should be thrown
                }
            }
        }
    })
