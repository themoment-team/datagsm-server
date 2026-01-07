package team.themoment.datagsm.authorization.domain.client.service

interface GetAvailableOauthScopesService {
    fun execute(): Set<String>
}
