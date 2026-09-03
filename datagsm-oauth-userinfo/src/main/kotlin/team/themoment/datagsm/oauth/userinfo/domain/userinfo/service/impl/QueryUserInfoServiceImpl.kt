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
        val scopeNames = currentUserProvider.getGrantedScopeNames()

        // self_read(deprecated)는 account_read + student_read를 합친 기존 동작과 동일하게 취급한다.
        val hasStudentRead = "student_read" in scopeNames || "self_read" in scopeNames
        val hasClubRead = "club_read" in scopeNames
        val hasProjectRead = "project_read" in scopeNames

        return AccountInfoResDto(
            id = account.id!!,
            email = account.email,
            role = account.role,
            status = account.status,
            objectType = account.objectType,
            isStudent = account.objectType == AccountObjectType.STUDENT,
            student = if (hasStudentRead) resolved.student else null,
            teacher = if (hasStudentRead) resolved.teacher else null,
            clubs = if (hasClubRead) resolved.clubs else emptyList(),
            projects = if (hasProjectRead) resolved.projects else emptyList(),
        )
    }
}
