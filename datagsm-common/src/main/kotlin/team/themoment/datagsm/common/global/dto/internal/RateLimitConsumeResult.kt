package team.themoment.datagsm.common.global.dto.internal

data class RateLimitConsumeResult(
    val consumed: Boolean,
    val remainingTokens: Long,
    val secondsToWaitForRefill: Long,
)
