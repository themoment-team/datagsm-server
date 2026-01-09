package team.themoment.datagsm.common.dto.global.internal

data class RateLimitConsumeResult(
    val consumed: Boolean,
    val remainingTokens: Long,
    val secondsToWaitForRefill: Long,
)
