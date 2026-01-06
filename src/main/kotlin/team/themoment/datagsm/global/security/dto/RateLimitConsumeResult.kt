package team.themoment.datagsm.global.security.dto

data class RateLimitConsumeResult(
    val consumed: Boolean,
    val remainingTokens: Long,
    val secondsToWaitForRefill: Long,
)
