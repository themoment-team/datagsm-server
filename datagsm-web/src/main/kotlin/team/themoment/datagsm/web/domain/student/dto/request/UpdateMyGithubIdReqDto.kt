package team.themoment.datagsm.web.domain.student.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateMyGithubIdReqDto(
    // Github 서비스 자체 ID 제약조건과 동일
    @field:Size(max = 39)
    @field:Pattern(regexp = "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,37}[a-zA-Z0-9])?$", message = "유효하지 않은 GitHub 아이디 형식입니다.")
    @field:JsonProperty("githubId")
    @param:Schema(description = "GitHub 아이디", example = "torvalds", maxLength = 39)
    val githubId: String?,
)
