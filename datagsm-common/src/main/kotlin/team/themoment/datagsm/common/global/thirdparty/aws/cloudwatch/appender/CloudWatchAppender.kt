package team.themoment.datagsm.common.global.thirdparty.aws.cloudwatch.appender

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Layout
import ch.qos.logback.core.UnsynchronizedAppenderBase
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

class CloudWatchAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    var logGroupName: String? = null
    var logStreamNamePrefix: String? = null
    var region: String = Region.AP_NORTHEAST_2.id()
    var maxBatchSize: Int = 50
    var maxBatchTimeMillis: Long = 10000
    var queueCapacity: Int = DEFAULT_QUEUE_CAPACITY
    var retentionTimeDays: Int = 30
    var shutdownTimeoutMillis: Long = 5000
    var maxRetries: Int = 3

    // 패턴의 %file:%line은 호출 스레드에서만 구할 수 있어, 큐에 넣기 전에 계산해 둔다.
    var includeCallerData: Boolean = true
    var layout: Layout<ILoggingEvent>? = null

    internal var clientFactory: (String) -> CloudWatchLogsClient = { regionId ->
        CloudWatchLogsClient
            .builder()
            .region(Region.of(regionId))
            .credentialsProvider(
                DefaultCredentialsProvider
                    .builder()
                    .build(),
            ).build()
    }

    private lateinit var cloudWatchClient: CloudWatchLogsClient
    private lateinit var logQueue: BlockingQueue<ILoggingEvent>
    private lateinit var activeLayout: Layout<ILoggingEvent>
    private val droppedEventCount = AtomicLong()
    private var writerThread: Thread? = null
    private var actualLogStreamName: String? = null

    @Volatile
    private var running = false

    override fun start() {
        if (logGroupName.isNullOrBlank()) {
            addError("logGroupName must be set")
            return
        }
        if (logStreamNamePrefix.isNullOrBlank()) {
            addError("logStreamNamePrefix must be set")
            return
        }
        actualLogStreamName = "$logStreamNamePrefix${UUID.randomUUID()}"
        try {
            activeLayout = layout ?: createDefaultLayout()
            logQueue = LinkedBlockingQueue(resolveQueueCapacity())
            cloudWatchClient = clientFactory(region)

            initializeLogGroup()
            initializeLogStream()

            running = true
            // non-daemon이면 기동 실패 등으로 stop()이 불리지 않을 때 JVM이 종료되지 못한다.
            writerThread =
                thread(name = "CloudWatchAppender-Writer-$name", isDaemon = true) {
                    runWriter()
                }

            super.start()
            addInfo("CloudWatchAppender started for log group: $logGroupName, stream: $actualLogStreamName")
        } catch (e: Exception) {
            addError("Failed to start CloudWatchAppender", e)
        }
    }

    override fun stop() {
        // 먼저 started를 내려 drain 이후에 들어와 버려지는 이벤트가 없게 한다.
        super.stop()
        // writer 대기와 남은 이벤트 전송이 함께 shutdownTimeoutMillis 안에 끝나도록 같은 마감 시각을 쓴다.
        val deadline = System.currentTimeMillis() + shutdownTimeoutMillis
        running = false
        writerThread?.interrupt()
        writerThread?.join(shutdownTimeoutMillis)

        // writer가 아직 전송 중이면 같은 이벤트·레이아웃·클라이언트를 함께 쓰게 되므로 손대지 않는다.
        // daemon 스레드라 JVM 종료를 막지 않는다.
        if (writerThread?.isAlive == true) {
            addWarn("Writer thread did not stop within $shutdownTimeoutMillis ms, skipping final flush")
            return
        }

        // 설정 누락으로 시작하지 않은 appender도 logback 종료 시 stop()이 호출된다.
        if (::logQueue.isInitialized) {
            try {
                flushLogs(deadline)
            } catch (e: Exception) {
                addError("Error flushing logs during shutdown", e)
            }
        }

        if (::cloudWatchClient.isInitialized) {
            try {
                cloudWatchClient.close()
            } catch (e: Exception) {
                addError("Error closing CloudWatch client", e)
            }
        }

        addInfo("CloudWatchAppender stopped")
    }

    override fun append(eventObject: ILoggingEvent) {
        if (!isStarted) {
            return
        }

        eventObject.prepareForDeferredProcessing()
        if (includeCallerData) {
            eventObject.callerData
        }

        // CloudWatch 장애 시 로그를 남기는 요청 스레드가 함께 멈추지 않도록 기다리지 않고 버린다.
        if (!logQueue.offer(eventObject)) {
            droppedEventCount.incrementAndGet()
        }
    }

    // 0 이하이면 LinkedBlockingQueue가 예외를 던져 appender가 아예 시작되지 못하므로 기본값으로 대신한다.
    private fun resolveQueueCapacity(): Int {
        if (queueCapacity > 0) return queueCapacity
        addWarn("queueCapacity must be positive but was $queueCapacity, using $DEFAULT_QUEUE_CAPACITY")
        return DEFAULT_QUEUE_CAPACITY
    }

    private fun createDefaultLayout(): Layout<ILoggingEvent> =
        PatternLayout().apply {
            context = this@CloudWatchAppender.context
            pattern = DEFAULT_PATTERN
            start()
        }

    private fun initializeLogGroup() {
        try {
            val request =
                CreateLogGroupRequest
                    .builder()
                    .logGroupName(logGroupName)
                    .build()
            cloudWatchClient.createLogGroup(request)
            addInfo("Created log group: $logGroupName")
        } catch (_: ResourceAlreadyExistsException) {
            addInfo("Log group already exists: $logGroupName")
        } catch (e: Exception) {
            addError("Failed to create log group: $logGroupName", e)
            throw e
        }

        if (retentionTimeDays > 0) {
            try {
                val retentionRequest =
                    PutRetentionPolicyRequest
                        .builder()
                        .logGroupName(logGroupName)
                        .retentionInDays(retentionTimeDays)
                        .build()
                cloudWatchClient.putRetentionPolicy(retentionRequest)
                addInfo("Set retention policy to $retentionTimeDays days for log group: $logGroupName")
            } catch (e: Exception) {
                addError("Failed to set retention policy for log group: $logGroupName", e)
            }
        }
    }

    private fun initializeLogStream() {
        try {
            val request =
                CreateLogStreamRequest
                    .builder()
                    .logGroupName(logGroupName)
                    .logStreamName(actualLogStreamName)
                    .build()
            cloudWatchClient.createLogStream(request)
            addInfo("Created log stream: $actualLogStreamName")
        } catch (_: ResourceAlreadyExistsException) {
            addInfo("Log stream already exists: $actualLogStreamName")
        } catch (e: Exception) {
            addError("Failed to create log stream: $actualLogStreamName", e)
            throw e
        }
    }

    private fun runWriter() {
        val batch = mutableListOf<ILoggingEvent>()
        var lastFlushTime = System.currentTimeMillis()

        while (running || logQueue.isNotEmpty()) {
            try {
                val event = logQueue.poll(1000, TimeUnit.MILLISECONDS)

                if (event != null) {
                    batch.add(event)
                }

                val now = System.currentTimeMillis()
                val shouldFlush =
                    batch.size >= maxBatchSize ||
                        (batch.isNotEmpty() && (now - lastFlushTime) >= maxBatchTimeMillis)

                if (shouldFlush) {
                    flushBatch(batch)
                    batch.clear()
                    lastFlushTime = now
                }
            } catch (_: InterruptedException) {
                addInfo("Writer thread interrupted, flushing remaining logs")
                if (!running) {
                    break
                }
            } catch (e: Exception) {
                addError("Error in writer thread", e)
            }
        }

        if (batch.isNotEmpty()) {
            try {
                flushBatch(batch)
            } catch (e: Exception) {
                addError("Error flushing final batch", e)
            }
        }
    }

    private fun flushLogs(deadline: Long) {
        val batch = mutableListOf<ILoggingEvent>()
        logQueue.drainTo(batch)
        if (batch.isNotEmpty() || droppedEventCount.get() > 0) {
            flushBatch(batch, deadline)
        }
    }

    internal fun flushBatch(
        batch: List<ILoggingEvent>,
        deadline: Long = Long.MAX_VALUE,
    ) {
        try {
            // 안내 전송이 실패하면 다음 전송에서 다시 알리도록, 보낸 뒤에만 카운트를 차감한다.
            val droppedCount = droppedEventCount.get()
            val droppedNotice = createDroppedEventsNotice(droppedCount)
            val logEvents =
                (
                    batch.map { event ->
                        InputLogEvent
                            .builder()
                            .timestamp(event.timeStamp)
                            .message(truncateToMaxEventBytes(activeLayout.doLayout(event)))
                            .build()
                    } + listOfNotNull(droppedNotice)
                ).sortedBy { it.timestamp() }

            // 요청 하나의 크기 상한을 넘으면 묶음 전체가 거절되므로 크기 기준으로 나눠 보낸다.
            val chunks = splitBySize(logEvents)
            chunks.forEachIndexed { index, chunk ->
                if (System.currentTimeMillis() >= deadline) {
                    val skippedCount = chunks.drop(index).sumOf { it.size }
                    addWarn("Skipped $skippedCount log events because the shutdown timeout elapsed")
                    return
                }
                val sent = putWithRetry(chunk)
                if (sent && chunk.any { it === droppedNotice }) {
                    droppedEventCount.addAndGet(-droppedCount)
                }
            }
        } catch (e: Exception) {
            addError("Failed to send log events to CloudWatch", e)
        }
    }

    private fun createDroppedEventsNotice(droppedCount: Long): InputLogEvent? {
        if (droppedCount == 0L) return null
        addWarn("Dropped $droppedCount log events because the queue was full")
        return InputLogEvent
            .builder()
            .timestamp(System.currentTimeMillis())
            .message("[CloudWatchAppender] [WARN] - Dropped $droppedCount log events because the queue was full")
            .build()
    }

    private fun putWithRetry(logEvents: List<InputLogEvent>): Boolean {
        try {
            repeat(maxRetries) { retryCount ->
                try {
                    val request =
                        PutLogEventsRequest
                            .builder()
                            .logGroupName(logGroupName)
                            .logStreamName(actualLogStreamName)
                            .logEvents(logEvents)
                            .build()

                    cloudWatchClient.putLogEvents(request)
                    return true
                } catch (e: ResourceNotFoundException) {
                    addError("Log group or stream not found, attempting to recreate", e)
                    initializeLogGroup()
                    initializeLogStream()
                    if (retryCount >= maxRetries - 1) throw e
                }
            }
        } catch (e: Exception) {
            addError("Failed to send log events to CloudWatch", e)
        }
        return false
    }

    companion object {
        private const val DEFAULT_PATTERN = "[%thread] [%level] %logger - %msg%n"
        private const val DEFAULT_QUEUE_CAPACITY = 10000

        // CloudWatch는 이벤트마다 메시지 바이트에 26바이트를 더해 요청 크기를 계산한다.
        private const val EVENT_OVERHEAD_BYTES = 26
        private const val MAX_EVENT_BYTES = 256 * 1024 - EVENT_OVERHEAD_BYTES
        private const val MAX_REQUEST_BYTES = 1024 * 1024
        private const val MAX_EVENTS_PER_REQUEST = 10000
        private const val TRUNCATED_SUFFIX = "...[truncated]"

        internal fun truncateToMaxEventBytes(
            message: String,
            maxBytes: Int = MAX_EVENT_BYTES,
        ): String {
            val bytes = message.toByteArray(Charsets.UTF_8)
            if (bytes.size <= maxBytes) return message

            val suffixBytes = TRUNCATED_SUFFIX.toByteArray(Charsets.UTF_8).size
            // 표시 문자열은 ASCII라 문자 수가 곧 바이트 수다.
            if (maxBytes <= suffixBytes) return TRUNCATED_SUFFIX.take(maxOf(maxBytes, 0))
            // 잘린 위치에 걸친 멀티바이트 문자는 버려서 깨진 문자가 남지 않게 한다.
            val decoder =
                Charsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.IGNORE)
            val kept = decoder.decode(ByteBuffer.wrap(bytes, 0, maxBytes - suffixBytes)).toString()
            return kept + TRUNCATED_SUFFIX
        }

        internal fun splitBySize(
            logEvents: List<InputLogEvent>,
            maxRequestBytes: Int = MAX_REQUEST_BYTES,
        ): List<List<InputLogEvent>> {
            val chunks = mutableListOf<List<InputLogEvent>>()
            var current = mutableListOf<InputLogEvent>()
            var currentBytes = 0

            logEvents.forEach { event ->
                val eventBytes = event.message().toByteArray(Charsets.UTF_8).size + EVENT_OVERHEAD_BYTES
                val exceeds = currentBytes + eventBytes > maxRequestBytes || current.size >= MAX_EVENTS_PER_REQUEST
                if (current.isNotEmpty() && exceeds) {
                    chunks.add(current)
                    current = mutableListOf()
                    currentBytes = 0
                }
                current.add(event)
                currentBytes += eventBytes
            }
            if (current.isNotEmpty()) chunks.add(current)
            return chunks
        }
    }
}
