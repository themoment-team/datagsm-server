package team.themoment.datagsm.common.domain.oauth.entity.constant

enum class PkceChallengeMethod(
    val value: String,
) {
    PLAIN("plain"),
    S256("S256"),
    ;

    companion object {
        fun from(value: String?): PkceChallengeMethod = value?.let { v -> entries.find { it.value == v } } ?: PLAIN

        fun fromOrNull(value: String?): PkceChallengeMethod? = value?.let { v -> entries.find { it.value == v } }
    }
}
