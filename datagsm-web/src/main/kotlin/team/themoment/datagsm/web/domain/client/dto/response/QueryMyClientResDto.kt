package team.themoment.datagsm.web.domain.client.dto.response

data class QueryMyClientResDto(
    val clients: List<ClientResDto>,
    val totalElements: Long,
)
