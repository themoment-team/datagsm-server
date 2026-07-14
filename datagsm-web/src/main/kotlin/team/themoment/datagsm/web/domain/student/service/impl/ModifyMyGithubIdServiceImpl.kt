package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMyGithubIdReqDto
import team.themoment.datagsm.web.domain.student.service.ModifyMyGithubIdService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class ModifyMyGithubIdServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : ModifyMyGithubIdService {
    @Transactional
    override fun execute(reqDto: UpdateMyGithubIdReqDto) {
        val student = currentUserProvider.getCurrentStudent()
        student.githubId = reqDto.githubId
    }
}
