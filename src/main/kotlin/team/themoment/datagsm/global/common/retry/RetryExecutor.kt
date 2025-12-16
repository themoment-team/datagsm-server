package team.themoment.datagsm.global.common.retry

import com.github.snowykte0426.peanut.butter.logging.logger

object RetryExecutor {
    fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelayMillis: Long = 5000,
        operation: () -> T,
    ): T {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxAttempts) {
            try {
                return operation()
            } catch (e: Exception) {
                attempt++
                lastException = e
                logger().error("Operation failed (attempt $attempt/$maxAttempts)", e)

                if (attempt < maxAttempts) {
                    Thread.sleep(initialDelayMillis * attempt)
                }
            }
        }

        throw lastException ?: IllegalStateException("Retry failed without exception")
    }
}
