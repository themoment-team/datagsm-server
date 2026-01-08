package team.themoment.datagsm.authorization.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.authorization.domain.account.dto.response.GetMyInfoResDto
import team.themoment.datagsm.authorization.domain.account.service.GetMyInfoService
import team.themoment.datagsm.authorization.global.security.authentication.type.AuthType
import team.themoment.datagsm.authorization.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class GetMyInfoServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : GetMyInfoService {
    @Transactional(readOnly = true)
    override fun execute(): GetMyInfoResDto {
        val principal = currentUserProvider.getPrincipal()
        if (principal.type == AuthType.API_KEY) {
            throw ExpectedException("API Key 인증은 해당 API를 지원하지 않습니다.", HttpStatus.FORBIDDEN)
        }
        val account = currentUserProvider.getCurrentAccount()

        return GetMyInfoResDto(
            id = account.id!!,
            email = account.email,
            role = account.role,
        )
    }
}
