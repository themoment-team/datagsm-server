package team.themoment.datagsm.domain.client.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateClientReqDto(
    @field:NotBlank
    @field:Size(max = 100)
    @param:Schema(description = "클라이언트 이름", example = "My OAuth Client", maxLength = 100)
    val name: String,
)
