package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.response.AccountInfoResDto
import team.themoment.datagsm.common.domain.student.dto.response.StudentResDto
import team.themoment.datagsm.web.domain.account.service.QueryMyInfoService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class QueryMyInfoServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : QueryMyInfoService {
    @Transactional(readOnly = true)
    override fun execute(): AccountInfoResDto {
        val account = currentUserProvider.getCurrentAccount()

        return AccountInfoResDto(
            id = account.id!!,
            email = account.email,
            role = account.role,
            isStudent = account.student != null,
            student = account.student?.let { StudentResDto.from(it) },
        )
    }
}
