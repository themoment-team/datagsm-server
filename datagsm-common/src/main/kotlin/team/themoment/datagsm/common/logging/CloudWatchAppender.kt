package team.themoment.datagsm.common.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import com.github.snowykte0426.peanut.butter.logging.logger
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidSequenceTokenException
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException
import java.util.UUID
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class CloudWatchAppender : UnsynchronizedAppenderBase<ILoggingEvent>() {
    var logGroupName: String? = null
    var logStreamNamePrefix: String? = null
    var region: String = Region.AP_NORTHEAST_2.id()
    var maxBatchSize: Int = 50
    var maxBatchTimeMillis: Long = 10000
    var maxBlockTimeMillis: Long = 5000
    var retentionTimeDays: Int = 30

    private lateinit var cloudWatchClient: CloudWatchLogsClient
    private val logQueue: BlockingQueue<ILoggingEvent> = LinkedBlockingQueue()
    private val sequenceToken = AtomicReference<String?>(null)
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
            cloudWatchClient =
                CloudWatchLogsClient
                    .builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                        DefaultCredentialsProvider
                            .builder()
                            .build(),
                    ).build()

            initializeLogGroup()
            initializeLogStream()

            running = true
            writerThread =
                thread(name = "CloudWatchAppender-Writer") {
                    runWriter()
                }

            super.start()
            addInfo("CloudWatchAppender started for log group: $logGroupName, stream: $actualLogStreamName")
        } catch (e: Exception) {
            addError("Failed to start CloudWatchAppender", e)
        }
    }

    override fun stop() {
        running = false
        writerThread?.interrupt()
        writerThread?.join(5000)

        try {
            flushLogs()
        } catch (e: Exception) {
            addError("Error flushing logs during shutdown", e)
        }

        try {
            cloudWatchClient.close()
        } catch (e: Exception) {
            addError("Error closing CloudWatch client", e)
        }

        super.stop()
        addInfo("CloudWatchAppender stopped")
    }

    override fun append(eventObject: ILoggingEvent) {
        if (!isStarted) {
            return
        }

        try {
            val success = logQueue.offer(eventObject, maxBlockTimeMillis, TimeUnit.MILLISECONDS)
            if (!success) {
                addWarn("Log queue is full, dropping log event")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            addError("Interrupted while adding log event to queue", e)
        }
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
        } catch (e: ResourceAlreadyExistsException) {
            addInfo("Log group already exists: $logGroupName")
            logger().warn("Log group $logGroupName already exists. ${e.message}")
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
        } catch (e: ResourceAlreadyExistsException) {
            addInfo("Log stream already exists: $actualLogStreamName")
            refreshSequenceToken()
            logger().warn("Log stream $actualLogStreamName already exists. ${e.message}")
        } catch (e: Exception) {
            addError("Failed to create log stream: $actualLogStreamName", e)
            throw e
        }
    }

    private fun refreshSequenceToken() {
        try {
            val request =
                DescribeLogStreamsRequest
                    .builder()
                    .logGroupName(logGroupName)
                    .logStreamNamePrefix(actualLogStreamName)
                    .build()

            val response = cloudWatchClient.describeLogStreams(request)
            val logStream = response.logStreams().firstOrNull { it.logStreamName() == actualLogStreamName }
            sequenceToken.set(logStream?.uploadSequenceToken())
        } catch (e: Exception) {
            addError("Failed to refresh sequence token", e)
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
            } catch (e: InterruptedException) {
                logger().info("Writer thread interrupted, flushing remaining logs, ${e.message}")
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

    private fun flushLogs() {
        val batch = mutableListOf<ILoggingEvent>()
        logQueue.drainTo(batch)
        if (batch.isNotEmpty()) {
            flushBatch(batch)
        }
    }

    private fun flushBatch(batch: List<ILoggingEvent>) {
        if (batch.isEmpty()) {
            return
        }

        try {
            val logEvents =
                batch
                    .map { event ->
                        InputLogEvent
                            .builder()
                            .timestamp(event.timeStamp)
                            .message(event.formattedMessage)
                            .build()
                    }.sortedBy { it.timestamp() }
            repeat(3) { retryCount ->
                try {
                    val requestBuilder =
                        PutLogEventsRequest
                            .builder()
                            .logGroupName(logGroupName)
                            .logStreamName(actualLogStreamName)
                            .logEvents(logEvents)

                    val currentToken = sequenceToken.get()
                    if (currentToken != null) {
                        requestBuilder.sequenceToken(currentToken)
                    }

                    val response = cloudWatchClient.putLogEvents(requestBuilder.build())
                    sequenceToken.set(response.nextSequenceToken())
                    return@repeat
                } catch (e: InvalidSequenceTokenException) {
                    sequenceToken.set(e.expectedSequenceToken())
                    if (retryCount >= 2) throw e
                } catch (e: ResourceNotFoundException) {
                    addError("Log group or stream not found, attempting to recreate", e)
                    initializeLogGroup()
                    initializeLogStream()
                    if (retryCount >= 2) throw e
                }
            }
        } catch (e: Exception) {
            addError("Failed to send log events to CloudWatch", e)
        }
    }
}
