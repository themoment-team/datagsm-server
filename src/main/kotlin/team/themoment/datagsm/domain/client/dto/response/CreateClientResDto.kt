package team.themoment.datagsm.domain.client.dto.response

data class CreateClientResDto(
    val clientId: String,
    val clientSecret: String,
    val name: String,
    val redirectUri: List<String>,
)
