package team.themoment.datagsm.oauth.authorization.global.template

enum class EmailTemplate(
    val subject: String,
    val bodyTemplate: String,
) {
    SIGNUP(
        subject = "DataGSM 인증 코드",
        bodyTemplate = "인증 코드는 %s 입니다. 5분 이내로 입력해주세요.",
    ),
    PASSWORD_RESET(
        subject = "DataGSM 비밀번호 재설정 코드",
        bodyTemplate = "비밀번호 재설정 인증 코드는 %s 입니다. 5분 이내로 입력해주세요.",
    ),
    ;

    fun formatBody(code: String): String = bodyTemplate.format(code)
}
