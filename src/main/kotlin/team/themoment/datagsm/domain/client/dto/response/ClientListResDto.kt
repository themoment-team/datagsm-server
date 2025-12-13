package team.themoment.datagsm.domain.client.dto.response

data class ClientListResDto(
    val totalPages: Int,
    val totalElements: Long,
    val clients: List<ClientResDto>,
)
