package team.themoment.datagsm.common.domain.project.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
data class ProjectIconUploadUrlResDto(
    @field:Schema(description = "S3에 직접 PUT 업로드할 presigned URL")
    val uploadUrl: String,
    @field:Schema(
        description = "업로드 완료 후 프로젝트 신청 시 전달할 오브젝트 키",
        example = "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png",
    )
    val iconKey: String,
    @field:Schema(description = "presigned URL 만료까지 남은 초", example = "300")
    val expiresInSeconds: Long,
)
