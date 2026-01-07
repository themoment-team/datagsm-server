package team.themoment.datagsm.authorization.domain.client.dto.response

data class QueryMyClientResDto(
    val clients: List<ClientResDto>,
    val totalElements: Long,
)
