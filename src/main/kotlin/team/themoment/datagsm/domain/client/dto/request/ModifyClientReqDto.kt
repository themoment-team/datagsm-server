package team.themoment.datagsm.domain.client.dto.request

data class ModifyClientReqDto(
    val name: String? = null,
    val redirectUri: List<String>? = null,
)
