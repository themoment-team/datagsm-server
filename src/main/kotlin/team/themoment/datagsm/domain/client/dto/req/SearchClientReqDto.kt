package team.themoment.datagsm.domain.client.dto.req

data class SearchClientReqDto(
    val name: String? = null,
    val page: Int,
    val size: Int,
)
