package team.themoment.datagsm.common.global.config

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import team.themoment.datagsm.common.global.common.discord.error.DiscordErrorNotificationService
import team.themoment.sdk.logging.logger.logger

@Configuration
@EnableAsync
class AsyncConfig(
    private val discordErrorNotificationProvider: ObjectProvider<DiscordErrorNotificationService>,
    private val environment: Environment,
) : AsyncConfigurer {
    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler =
        AsyncUncaughtExceptionHandler { ex, method, _ ->
            val methodName = "${method.declaringClass.simpleName}.${method.name}"
            logger().error("Unexpected exception occurred in async method {}", methodName, ex)

            discordErrorNotificationProvider.ifAvailable { discordErrorNotificationService ->
                discordErrorNotificationService.notifyError(
                    exception = ex,
                    context = "An unexpected exception occurred in an async method.",
                    additionalInfo =
                        mapOf(
                            "Thread" to Thread.currentThread().name,
                            "Method" to methodName,
                            "Profile" to getActiveProfile(),
                        ),
                )
            }
        }

    private fun getActiveProfile(): String = environment.activeProfiles.firstOrNull() ?: "default"
}
