package team.themoment.datagsm.domain.account.dto.request

data class CreateAccountReqDto(
    val email: String,
    val password: String,
    val code: String,
)
