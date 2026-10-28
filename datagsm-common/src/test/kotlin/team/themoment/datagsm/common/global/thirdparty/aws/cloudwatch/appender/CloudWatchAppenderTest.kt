package team.themoment.datagsm.common.global.thirdparty.aws.cloudwatch.appender

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.util.LogbackMDCAdapter
import ch.qos.logback.core.status.Status
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CloudWatchAppenderTest :
    DescribeSpec({
        lateinit var loggerContext: LoggerContext
        lateinit var client: CloudWatchLogsClient
        lateinit var sentRequests: MutableList<PutLogEventsRequest>

        fun sentMessages(): List<String> = sentRequests.flatMap { request -> request.logEvents().map { it.message() } }

        fun createAppender(configure: CloudWatchAppender.() -> Unit = {}): CloudWatchAppender =
            CloudWatchAppender().apply {
                context = loggerContext
                name = "test"
                logGroupName = "test-group"
                logStreamNamePrefix = "test-"
                maxBatchTimeMillis = 50
                clientFactory = { client }
                configure()
            }

        fun event(
            level: Level,
            message: String,
            throwable: Throwable? = null,
        ): LoggingEvent =
            LoggingEvent(
                Logger::class.java.name,
                loggerContext.getLogger("team.themoment.Sample"),
                level,
                message,
                throwable,
                null,
            )

        beforeEach {
            loggerContext = LoggerContext().apply { mdcAdapter = LogbackMDCAdapter() }
            sentRequests = Collections.synchronizedList(mutableListOf())
            client = mockk(relaxed = true)
            val request = slot<PutLogEventsRequest>()
            every { client.putLogEvents(capture(request)) } answers {
                sentRequests.add(request.captured)
                PutLogEventsResponse.builder().build()
            }
        }

        describe("CloudWatchAppender 클래스의") {
            describe("전송 형식은") {
                it("예외가 포함된 로그를 레벨과 스택트레이스까지 포함해 보내야 한다") {
                    // Given
                    val appender = createAppender()
                    appender.start()

                    // When
                    appender.doAppend(event(Level.ERROR, "Unexpected exception occurred", IllegalStateException("boom")))
                    appender.stop()

                    // Then
                    val message = sentMessages().single()
                    message shouldContain "[ERROR]"
                    message shouldContain "Unexpected exception occurred"
                    message shouldContain "java.lang.IllegalStateException: boom"
                    message shouldContain "\tat "
                }

                it("logback 설정의 <layout>을 사용해야 한다") {
                    // Given
                    val xml =
                        """
                        <configuration>
                          <appender name="cw" class="${CloudWatchAppender::class.java.name}">
                            <layout>
                              <pattern>[%level] %msg%n</pattern>
                            </layout>
                          </appender>
                          <root level="INFO">
                            <appender-ref ref="cw"/>
                          </root>
                        </configuration>
                        """.trimIndent()

                    // When
                    JoranConfigurator()
                        .apply { context = loggerContext }
                        .doConfigure(xml.byteInputStream())

                    // Then
                    // logGroupName이 없어 시작하지 않으므로 AWS 호출 없이 layout 주입만 확인한다.
                    val appender =
                        loggerContext
                            .getLogger(Logger.ROOT_LOGGER_NAME)
                            .getAppender("cw") as CloudWatchAppender
                    val layout = appender.layout
                    layout.shouldBeInstanceOf<PatternLayout>()
                    layout.pattern shouldBe "[%level] %msg%n"
                    layout.isStarted shouldBe true
                }
            }

            describe("writer 스레드는") {
                it("JVM 종료를 막지 않도록 daemon이어야 한다") {
                    // Given
                    val appender = createAppender()

                    // When
                    appender.start()

                    // Then
                    val writer = Thread.getAllStackTraces().keys.single { it.name == "CloudWatchAppender-Writer-test" }
                    writer.isDaemon shouldBe true
                    appender.stop()
                }
            }

            describe("writer가 종료 대기 시간 안에 끝나지 않으면") {
                it("전송 중인 writer와 겹치지 않도록 마지막 flush와 클라이언트 종료를 건너뛰어야 한다") {
                    // Given
                    val sendStarted = CountDownLatch(1)
                    val releaseSend = CountDownLatch(1)
                    every { client.putLogEvents(any<PutLogEventsRequest>()) } answers {
                        sendStarted.countDown()
                        // 인터럽트를 무시하고 전송을 계속하는 상황을 흉내 낸다.
                        while (releaseSend.count > 0) {
                            runCatching { releaseSend.await(5, TimeUnit.SECONDS) }
                        }
                        PutLogEventsResponse.builder().build()
                    }
                    val appender =
                        createAppender {
                            maxBatchSize = 1
                            shutdownTimeoutMillis = 100
                        }
                    appender.start()
                    appender.doAppend(event(Level.INFO, "slow"))
                    sendStarted.await(5, TimeUnit.SECONDS) shouldBe true

                    // When
                    appender.stop()

                    // Then
                    verify(exactly = 0) { client.close() }
                    verify(exactly = 1) { client.putLogEvents(any<PutLogEventsRequest>()) }
                    releaseSend.countDown()
                }
            }

            describe("종료 마감 시각이 지나면") {
                it("남은 전송 묶음을 보내지 않고 건너뛴 개수를 남겨야 한다") {
                    // Given
                    val appender = createAppender()
                    appender.start()
                    // 이벤트 하나가 상한(약 256KB)까지 잘려 요청 하나에 4개씩만 들어가므로 3개 묶음으로 나뉜다.
                    val events = (1..12).map { event(Level.INFO, "x".repeat(300 * 1024)) }

                    // When
                    appender.flushBatch(events, deadline = System.currentTimeMillis())

                    // Then
                    sentRequests shouldHaveSize 0
                    loggerContext.statusManager.copyOfStatusList
                        .single { it.message.startsWith("Skipped") }
                        .message shouldBe "Skipped 12 log events because the shutdown timeout elapsed"
                    appender.stop()
                }
            }

            describe("설정 누락으로 시작하지 못한 appender를 종료하면") {
                it("오류 상태를 남기지 않아야 한다") {
                    // Given
                    val appender = createAppender { logGroupName = null }
                    appender.start()
                    val errorCountBeforeStop =
                        loggerContext.statusManager.copyOfStatusList.count { it.level == Status.ERROR }

                    // When
                    appender.stop()

                    // Then
                    loggerContext.statusManager.copyOfStatusList.count { it.level == Status.ERROR } shouldBe
                        errorCountBeforeStop
                }
            }

            describe("큐가 가득 차면") {
                it("기다리지 않고 버린 뒤 버린 개수를 다음 전송에 남겨야 한다") {
                    // Given
                    val sendStarted = CountDownLatch(1)
                    val releaseSend = CountDownLatch(1)
                    every { client.putLogEvents(any<PutLogEventsRequest>()) } answers {
                        sentRequests.add(firstArg())
                        sendStarted.countDown()
                        releaseSend.await(5, TimeUnit.SECONDS)
                        PutLogEventsResponse.builder().build()
                    }
                    val appender =
                        createAppender {
                            maxBatchSize = 1
                            queueCapacity = 1
                        }
                    appender.start()
                    appender.doAppend(event(Level.INFO, "first"))
                    sendStarted.await(5, TimeUnit.SECONDS) shouldBe true

                    // When
                    val startedAt = System.nanoTime()
                    appender.doAppend(event(Level.INFO, "second"))
                    appender.doAppend(event(Level.INFO, "third"))
                    val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
                    releaseSend.countDown()
                    appender.stop()

                    // Then
                    elapsedMillis.toInt() shouldBeLessThanOrEqual 500
                    val messages = sentMessages()
                    messages.filter { it.contains("second") } shouldHaveSize 1
                    messages.filter { it.contains("third") } shouldHaveSize 0
                    messages.single { it.contains("Dropped") } shouldContain "Dropped 1 log events"
                }

                it("버린 개수 안내 전송이 실패하면 다음 전송에서 다시 알려야 한다") {
                    // Given
                    val sendStarted = CountDownLatch(1)
                    val releaseSend = CountDownLatch(1)
                    var noticeFailed = false
                    every { client.putLogEvents(any<PutLogEventsRequest>()) } answers {
                        val request = firstArg<PutLogEventsRequest>()
                        if (request.logEvents().any { it.message().contains("Dropped") } && !noticeFailed) {
                            noticeFailed = true
                            throw IllegalStateException("CloudWatch unavailable")
                        }
                        sentRequests.add(request)
                        sendStarted.countDown()
                        releaseSend.await(5, TimeUnit.SECONDS)
                        PutLogEventsResponse.builder().build()
                    }
                    val appender =
                        createAppender {
                            maxBatchSize = 1
                            queueCapacity = 1
                        }
                    appender.start()
                    appender.doAppend(event(Level.INFO, "first"))
                    sendStarted.await(5, TimeUnit.SECONDS) shouldBe true
                    appender.doAppend(event(Level.INFO, "second"))
                    appender.doAppend(event(Level.INFO, "third"))

                    // When
                    releaseSend.countDown()
                    appender.stop()

                    // Then
                    noticeFailed shouldBe true
                    sentMessages().single { it.contains("Dropped") } shouldContain "Dropped 1 log events"
                }
            }
        }

        describe("truncateToMaxEventBytes 메서드는") {
            it("상한을 넘는 메시지를 상한 이하로 자르고 표시를 붙여야 한다") {
                // Given
                val message = "가".repeat(100)

                // When
                val truncated = CloudWatchAppender.truncateToMaxEventBytes(message, maxBytes = 50)

                // Then
                truncated.toByteArray(Charsets.UTF_8).size shouldBeLessThanOrEqual 50
                truncated shouldEndWith "...[truncated]"
                truncated.removeSuffix("...[truncated]").all { it == '가' } shouldBe true
            }

            it("상한 이하 메시지는 그대로 반환해야 한다") {
                CloudWatchAppender.truncateToMaxEventBytes("short", maxBytes = 50) shouldBe "short"
            }
        }

        describe("splitBySize 메서드는") {
            it("요청 크기 상한을 넘지 않도록 이벤트를 나눠야 한다") {
                // Given
                val events =
                    (1..3).map {
                        InputLogEvent
                            .builder()
                            .timestamp(it.toLong())
                            .message("x".repeat(40))
                            .build()
                    }

                // When
                val chunks = CloudWatchAppender.splitBySize(events, maxRequestBytes = 100)

                // Then
                chunks.map { it.size } shouldBe listOf(1, 1, 1)
            }
        }
    })
