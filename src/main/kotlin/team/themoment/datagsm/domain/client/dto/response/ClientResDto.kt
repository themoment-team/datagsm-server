package team.themoment.datagsm.domain.client.dto.response

data class ClientResDto(
    val id: String,
    val name: String,
    val redirectUrl: List<String>,
)
