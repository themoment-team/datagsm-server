package team.themoment.datagsm.oauth.authorization.domain.oauth.service

interface IssueAuthorizationCodeService {
    fun execute(
        email: String,
        clientId: String,
        redirectUri: String,
        state: String?,
        codeChallenge: String?,
        codeChallengeMethod: String?,
        scopes: Set<String>,
    ): String
}
