package team.themoment.datagsm.web.global.dto

data class RateLimitConsumeResult(
    val consumed: Boolean,
    val remainingTokens: Long,
    val secondsToWaitForRefill: Long,
)
