package team.themoment.datagsm.oauth.userinfo.domain.userinfo.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.common.domain.account.entity.constant.AccountObjectType
import team.themoment.datagsm.common.domain.account.resolver.AccountObjectResolver
import team.themoment.datagsm.common.domain.client.entity.constant.OAuthScope
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
        val scopeNames = currentUserProvider.getGrantedScopeNames()

        // self_read(deprecated)는 account_read + student_read를 합친 기존 동작과 동일하게 취급한다.
        val hasStudentRead = OAuthScope.STUDENT_READ in scopeNames || OAuthScope.SELF_READ in scopeNames
        val hasClubRead = OAuthScope.CLUB_READ in scopeNames
        val hasProjectRead = OAuthScope.PROJECT_READ in scopeNames

        // 부여되지 않은 스코프의 club/project 조회는 리졸버가 아예 수행하지 않도록 건너뛴다.
        val resolved = accountObjectResolver.resolve(account, includeClubs = hasClubRead, includeProjects = hasProjectRead)

        // StudentResDto에는 majorClub/autonomousClub이 포함되어 있어, club_read가 없으면 clubs 필드뿐 아니라
        // student 안의 동아리 정보도 함께 가려야 club_read 스코프가 실제로 의미를 가진다.
        val student =
            if (hasStudentRead) {
                resolved.student?.let { if (hasClubRead) it else it.copy(majorClub = null, autonomousClub = null) }
            } else {
                null
            }

        return AccountInfoResDto(
            id = account.id!!,
            email = account.email,
            role = account.role,
            status = account.status,
            objectType = account.objectType,
            isStudent = account.objectType == AccountObjectType.STUDENT,
            student = student,
            teacher = if (hasStudentRead) resolved.teacher else null,
            clubs = if (hasClubRead) resolved.clubs else emptyList(),
            projects = if (hasProjectRead) resolved.projects else emptyList(),
        )
    }
}
