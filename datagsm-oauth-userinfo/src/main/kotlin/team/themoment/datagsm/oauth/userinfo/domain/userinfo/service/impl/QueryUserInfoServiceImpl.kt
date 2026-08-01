package team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.resolver.AccountObjectResolver
import team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.QueryUserInfoService
import team.themoment.datagsm.oauth.userinfo.global.security.provider.CurrentUserProvider

@Service
class QueryUserInfoServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val accountObjectResolver: AccountObjectResolver,
) : QueryUserInfoService {
    @Transactional(readOnly = true)
    override fun execute(): AccountInfoResDto {
        val account = currentUserProvider.getCurrentAccount()
        val resolved = accountObjectResolver.resolve(account)

        return AccountInfoResDto(
            id = account.id!!,
            email = account.email,
            role = account.role,
            status = account.status,
            objectType = account.objectType,
            isStudent = account.objectType == AccountObjectType.STUDENT,
            student = resolved.student,
            teacher = resolved.teacher,
        )
    }
}
