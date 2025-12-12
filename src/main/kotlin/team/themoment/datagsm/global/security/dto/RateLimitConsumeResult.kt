package team.themoment.datagsm.global.security.dto

data class RateLimitConsumeResult(
    val consumed: Boolean,
    val remainingTokens: Long,
    val nanosToWaitForRefill: Long,
) {
    val secondsToWaitForRefill: Long
        get() = if (consumed) 0 else nanosToWaitForRefill / 1_000_000_000
}
