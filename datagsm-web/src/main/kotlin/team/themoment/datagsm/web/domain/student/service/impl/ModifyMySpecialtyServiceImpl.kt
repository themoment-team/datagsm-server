package team.themoment.datagsm.web.domain.student.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.web.domain.student.dto.request.UpdateMySpecialtyReqDto
import team.themoment.datagsm.web.domain.student.service.ModifyMySpecialtyService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider

@Service
class ModifyMySpecialtyServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
) : ModifyMySpecialtyService {
    @Transactional
    override fun execute(reqDto: UpdateMySpecialtyReqDto) {
        val student = currentUserProvider.getCurrentStudent()
        student.specialty = reqDto.specialty
    }
}
