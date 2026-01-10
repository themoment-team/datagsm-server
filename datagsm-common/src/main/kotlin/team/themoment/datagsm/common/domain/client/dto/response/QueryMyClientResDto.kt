package team.themoment.datagsm.common.domain.client.dto.response

data class QueryMyClientResDto(
    val clients: List<ClientResDto>,
    val totalElements: Long,
)
