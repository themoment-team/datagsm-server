package team.themoment.datagsm.common.dto.client.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateClientReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @param:Schema(description = "클라이언트 이름", example = "My OAuth Client", maxLength = 100)
    val name: String,
    @field:Size(min = 1)
    @param:Schema(description = "Oauth Client에서 요청할 권한 목록", example = "[\"self:read\"]")
    val scopes: Set<String>,
)
