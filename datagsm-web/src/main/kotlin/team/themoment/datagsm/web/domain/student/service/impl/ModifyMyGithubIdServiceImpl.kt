package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMyGithubIdReqDto
import team.themoment.datagsm.web.domain.student.service.ModifyMyGithubIdService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class ModifyMyGithubIdServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : ModifyMyGithubIdService {
    @Transactional
    override fun execute(reqDto: UpdateMyGithubIdReqDto) {
        val account = currentUserProvider.getCurrentAccount()
        val student =
            account.student
                ?: throw ExpectedException("학생 정보가 연결되지 않은 계정입니다.", HttpStatus.FORBIDDEN)
        student.githubId = reqDto.githubId
    }
}
