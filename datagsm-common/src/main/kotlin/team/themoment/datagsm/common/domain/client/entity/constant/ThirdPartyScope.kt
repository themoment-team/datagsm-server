package team.themoment.datagsm.common.domain.client.entity.constant

class ThirdPartyScope(
    val applicationId: String,
    val scopeName: String,
    override val description: String,
) : OAuthScope("$applicationId:$scopeName", description) {
    companion object {
        fun fromScopeString(scopeStr: String): ThirdPartyScope? {
            val colonIdx = scopeStr.indexOf(':')
            if (colonIdx <= 0 || colonIdx == scopeStr.lastIndex) return null
            return ThirdPartyScope(
                applicationId = scopeStr.substring(0, colonIdx),
                scopeName = scopeStr.substring(colonIdx + 1),
                description = "",
            )
        }
    }
}
