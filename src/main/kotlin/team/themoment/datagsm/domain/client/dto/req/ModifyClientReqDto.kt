package team.themoment.datagsm.domain.client.dto.req

data class ModifyClientReqDto(
    val name: String? = null,
    val redirectUri: List<String>? = null,
)
