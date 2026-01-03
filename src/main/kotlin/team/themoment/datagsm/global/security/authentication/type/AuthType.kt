package team.themoment.datagsm.global.security.authentication.type
enum class AuthType {
    //DataGSM 웹페이지 JWT
    INTERNAL_JWT,
    //Oauth를 통해 발급받은 JWT
    OAUTH_JWT,
    //API 사용을 위한 API Key
    API_KEY,
}
