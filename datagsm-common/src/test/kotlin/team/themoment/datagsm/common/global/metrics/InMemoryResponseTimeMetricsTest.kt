package team.themoment.datagsm.common.global.metrics

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import team.themoment.datagsm.common.global.data.HealthCheckEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class InMemoryResponseTimeMetricsTest :
    DescribeSpec({

        describe("InMemoryResponseTimeMetrics 클래스의") {

            lateinit var metricsCollector: InMemoryResponseTimeMetrics
            lateinit var healthCheckEnvironment: HealthCheckEnvironment

            beforeEach {
                healthCheckEnvironment =
                    HealthCheckEnvironment(
                        metrics = HealthCheckEnvironment.Metrics(enabled = true, sampleSize = 1000),
                        thresholds =
                            HealthCheckEnvironment.Thresholds(
                                warnP95Ms = 500,
                                criticalP95Ms = 1000,
                                warnAvgMs = 300,
                                criticalAvgMs = 800,
                                errorRateWarn = 0.05,
                                errorRateCritical = 0.10,
                            ),
                        excludedPaths = listOf("/v1/health"),
                    )
                metricsCollector = InMemoryResponseTimeMetrics(healthCheckEnvironment)
            }

            describe("recordResponseTime 메서드는") {

                context("메트릭이 활성화되어 있을 때") {
                    it("응답 시간을 기록해야 한다") {
                        metricsCollector.recordResponseTime(100, 200)
                        metricsCollector.recordResponseTime(200, 200)
                        metricsCollector.recordResponseTime(300, 200)

                        val metrics = metricsCollector.getMetrics()
                        metrics.sampleSize shouldBe 3
                        metrics.avgMs shouldBe 200.0
                    }
                }

                context("메트릭이 비활성화되어 있을 때") {
                    it("응답 시간을 기록하지 않아야 한다") {
                        val disabledEnvironment =
                            healthCheckEnvironment.copy(
                                metrics = HealthCheckEnvironment.Metrics(enabled = false),
                            )
                        val disabledCollector = InMemoryResponseTimeMetrics(disabledEnvironment)

                        disabledCollector.recordResponseTime(100, 200)

                        val metrics = disabledCollector.getMetrics()
                        metrics.sampleSize shouldBe 0
                    }
                }

                context("샘플 크기를 초과하는 요청이 들어올 때") {
                    it("오래된 데이터를 제거하고 새 데이터를 추가해야 한다") {
                        val smallEnvironment =
                            healthCheckEnvironment.copy(
                                metrics = HealthCheckEnvironment.Metrics(enabled = true, sampleSize = 5),
                            )
                        val smallCollector = InMemoryResponseTimeMetrics(smallEnvironment)

                        repeat(10) { i ->
                            smallCollector.recordResponseTime((i + 1).toLong() * 100, 200)
                        }

                        val metrics = smallCollector.getMetrics()
                        metrics.sampleSize shouldBe 5
                        metrics.avgMs shouldBe 800.0
                    }
                }

                context("5xx 에러 응답이 포함될 때") {
                    it("에러율을 올바르게 계산해야 한다") {
                        metricsCollector.recordResponseTime(100, 200)
                        metricsCollector.recordResponseTime(200, 200)
                        metricsCollector.recordResponseTime(300, 500)
                        metricsCollector.recordResponseTime(400, 503)

                        val metrics = metricsCollector.getMetrics()
                        metrics.sampleSize shouldBe 4
                        metrics.errorRate shouldBe 0.5
                    }
                }
            }

            describe("getMetrics 메서드는") {

                context("데이터가 없을 때") {
                    it("모든 값이 0인 메트릭을 반환해야 한다") {
                        val metrics = metricsCollector.getMetrics()

                        metrics.sampleSize shouldBe 0
                        metrics.avgMs shouldBe 0.0
                        metrics.p50Ms shouldBe 0
                        metrics.p95Ms shouldBe 0
                        metrics.p99Ms shouldBe 0
                        metrics.errorRate shouldBe 0.0
                    }
                }

                context("충분한 데이터가 있을 때") {
                    it("백분위수를 올바르게 계산해야 한다") {
                        repeat(100) { i ->
                            metricsCollector.recordResponseTime((i + 1).toLong(), 200)
                        }

                        val metrics = metricsCollector.getMetrics()
                        metrics.sampleSize shouldBe 100
                        metrics.p50Ms shouldBe 50
                        metrics.p95Ms shouldBe 95
                        metrics.p99Ms shouldBe 99
                    }
                }

                context("평균 응답 시간을 계산할 때") {
                    it("정확한 평균값을 반환해야 한다") {
                        metricsCollector.recordResponseTime(100, 200)
                        metricsCollector.recordResponseTime(200, 200)
                        metricsCollector.recordResponseTime(300, 200)

                        val metrics = metricsCollector.getMetrics()
                        metrics.avgMs shouldBe 200.0
                    }
                }
            }

            describe("스레드 안전성") {

                context("여러 스레드에서 동시에 기록할 때") {
                    it("데이터 손실 없이 모든 요청을 기록해야 한다") {
                        val threadCount = 10
                        val recordsPerThread = 100
                        val executor = Executors.newFixedThreadPool(threadCount)
                        val latch = CountDownLatch(threadCount)

                        repeat(threadCount) { threadIndex ->
                            executor.submit {
                                try {
                                    repeat(recordsPerThread) { i ->
                                        metricsCollector.recordResponseTime(
                                            (threadIndex * recordsPerThread + i + 1).toLong(),
                                            200,
                                        )
                                    }
                                } finally {
                                    latch.countDown()
                                }
                            }
                        }

                        latch.await()
                        executor.shutdown()

                        val metrics = metricsCollector.getMetrics()
                        metrics.sampleSize shouldBe (threadCount * recordsPerThread)
                        metrics.avgMs shouldBeGreaterThan 0.0
                    }
                }
            }
        }
    })
