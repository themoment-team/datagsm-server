package team.themoment.datagsm.domain.client.dto.res

data class CreateClientResDto(
    val clientId: String,
    val clientSecret: String,
    val name: String,
    val redirectUri: List<String>,
)
