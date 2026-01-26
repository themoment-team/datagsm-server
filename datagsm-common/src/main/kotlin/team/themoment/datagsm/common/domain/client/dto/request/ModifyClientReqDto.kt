package team.themoment.datagsm.common.domain.client.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class ModifyClientReqDto(
    @field:Size(max = 100)
    @param:Schema(description = "클라이언트 이름 (수정할 경우에만 포함)", example = "Updated Client Name", maxLength = 100)
    val name: String? = null,
    @param:Schema(
        description = "리다이렉트 URL 목록 (수정할 경우에만 포함)",
        example = "[\"https://example.com/callback\", \"https://app.example.com/oauth/callback\"]",
    )
    val redirectUrls: Set<
        @NotBlank(message = "Redirect URL은 필수입니다.")
        @URL(message = "Redirect URL 형식이 올바르지 않습니다.")
        String,
    >? = null,
)
