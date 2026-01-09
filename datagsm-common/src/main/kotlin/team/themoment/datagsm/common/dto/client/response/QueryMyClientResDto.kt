package team.themoment.datagsm.common.dto.client.response

data class QueryMyClientResDto(
    val clients: List<ClientResDto>,
    val totalElements: Long,
)
