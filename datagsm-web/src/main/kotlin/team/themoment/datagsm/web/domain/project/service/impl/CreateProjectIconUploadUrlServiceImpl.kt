package team.themoment.datagsm.web.domain.project.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.project.dto.request.CreateProjectIconUploadUrlReqDto
import team.themoment.datagsm.common.domain.project.dto.response.ProjectIconUploadUrlResDto
import team.themoment.datagsm.web.domain.project.service.CreateProjectIconUploadUrlService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.web.global.security.service.IconUploadRateLimitService
import team.themoment.datagsm.web.global.storage.ProjectIconStorage

@Service
class CreateProjectIconUploadUrlServiceImpl(
    private val projectIconStorage: ProjectIconStorage,
    private val iconUploadRateLimitService: IconUploadRateLimitService,
    private val currentUserProvider: CurrentUserProvider,
) : CreateProjectIconUploadUrlService {
    override fun execute(reqDto: CreateProjectIconUploadUrlReqDto): ProjectIconUploadUrlResDto {
        iconUploadRateLimitService.ensureNotExceeded(currentUserProvider.getCurrentUserEmail())

        val uploadTarget = projectIconStorage.createUploadUrl(reqDto.contentType, reqDto.contentLength)

        return ProjectIconUploadUrlResDto(
            uploadUrl = uploadTarget.uploadUrl,
            iconKey = uploadTarget.iconKey,
            expiresInSeconds = uploadTarget.expiresInSeconds,
        )
    }
}
