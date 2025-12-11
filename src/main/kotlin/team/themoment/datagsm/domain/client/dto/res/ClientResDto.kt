package team.themoment.datagsm.domain.client.dto.res

data class ClientResDto(
    val id: String,
    val name: String,
    val redirectUrl: List<String>,
)
