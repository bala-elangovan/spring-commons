package io.github.balaelangovan.spring.webmvc.starter.async

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.slf4j.MDC
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class MdcTaskDecoratorTest :
    DescribeSpec({

        val decorator = MdcTaskDecorator()

        afterEach {
            MDC.clear()
        }

        describe("MdcTaskDecorator") {
            it("should propagate MDC context to decorated runnable") {
                val capturedValue = AtomicReference<String?>()
                val latch = CountDownLatch(1)

                MDC.put("testKey", "testValue")

                val decoratedRunnable = decorator.decorate {
                    capturedValue.set(MDC.get("testKey"))
                    latch.countDown()
                }

                // Run in a new thread to simulate async execution
                Thread(decoratedRunnable).start()
                latch.await(1, TimeUnit.SECONDS)

                capturedValue.get() shouldBe "testValue"
            }

            it("should propagate multiple MDC values") {
                val capturedValues = mutableMapOf<String, String?>()
                val latch = CountDownLatch(1)

                MDC.put("key1", "value1")
                MDC.put("key2", "value2")
                MDC.put("key3", "value3")

                val decoratedRunnable = decorator.decorate {
                    capturedValues["key1"] = MDC.get("key1")
                    capturedValues["key2"] = MDC.get("key2")
                    capturedValues["key3"] = MDC.get("key3")
                    latch.countDown()
                }

                Thread(decoratedRunnable).start()
                latch.await(1, TimeUnit.SECONDS)

                capturedValues["key1"] shouldBe "value1"
                capturedValues["key2"] shouldBe "value2"
                capturedValues["key3"] shouldBe "value3"
            }

            it("should clear MDC after runnable completes") {
                val mdcAfterExecution = AtomicReference<Map<String, String>?>()
                val latch = CountDownLatch(1)

                MDC.put("testKey", "testValue")

                val decoratedRunnable = decorator.decorate {
                    // Execution happens here
                }

                Thread {
                    decoratedRunnable.run()
                    mdcAfterExecution.set(MDC.getCopyOfContextMap())
                    latch.countDown()
                }.start()

                latch.await(1, TimeUnit.SECONDS)

                // MDC should be cleared after runnable completes
                val contextMap = mdcAfterExecution.get()
                (contextMap == null || contextMap.isEmpty()) shouldBe true
            }

            it("should handle empty MDC context") {
                MDC.clear()
                val capturedValue = AtomicReference<String?>("initial")
                val latch = CountDownLatch(1)

                val decoratedRunnable = decorator.decorate {
                    capturedValue.set(MDC.get("nonExistentKey"))
                    latch.countDown()
                }

                Thread(decoratedRunnable).start()
                latch.await(1, TimeUnit.SECONDS)

                capturedValue.get() shouldBe null
            }

            it("should clear MDC even if runnable throws exception") {
                val mdcAfterExecution = AtomicReference<Map<String, String>?>()
                val latch = CountDownLatch(1)

                MDC.put("testKey", "testValue")

                val decoratedRunnable = decorator.decorate {
                    throw RuntimeException("Test exception")
                }

                Thread {
                    try {
                        decoratedRunnable.run()
                    } catch (_: RuntimeException) {
                        // Expected
                    }
                    mdcAfterExecution.set(MDC.getCopyOfContextMap())
                    latch.countDown()
                }.start()

                latch.await(1, TimeUnit.SECONDS)

                val contextMap = mdcAfterExecution.get()
                contextMap.isNullOrEmpty() shouldBe true
            }
        }
    })
