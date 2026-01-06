package team.themoment.datagsm.domain.client.service

interface GetAvailableOauthScopesService {
    fun execute(): Set<String>
}
