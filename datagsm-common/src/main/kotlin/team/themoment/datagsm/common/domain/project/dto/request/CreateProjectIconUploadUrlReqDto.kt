package team.themoment.datagsm.common.domain.project.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateProjectIconUploadUrlReqDto(
    @field:NotBlank
    @param:Schema(
        description = "업로드할 이미지의 Content-Type",
        example = "image/png",
        allowableValues = ["image/png", "image/jpeg", "image/webp", "image/gif"],
    )
    val contentType: String,
    @field:Positive
    @field:Max(5 * 1024 * 1024)
    @param:Schema(description = "업로드할 이미지의 바이트 크기 (최대 5MB)", example = "204800")
    val contentLength: Long,
)
