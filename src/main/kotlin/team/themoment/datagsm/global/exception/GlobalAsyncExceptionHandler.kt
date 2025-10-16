package team.themoment.datagsm.global.exception

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class GlobalAsyncExceptionHandler : AsyncUncaughtExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalAsyncExceptionHandler::class.java)

    override fun handleUncaughtException(
        ex: Throwable,
        method: Method,
        vararg params: Any?,
    ) {
        logger.error("[ASYNC-DISCORD-ERROR] method: {}, exception: {}", method.name, ex)
    }
}
