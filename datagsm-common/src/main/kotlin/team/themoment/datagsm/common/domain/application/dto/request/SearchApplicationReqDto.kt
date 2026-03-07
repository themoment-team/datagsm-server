package team.themoment.datagsm.common.domain.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

data class SearchApplicationReqDto(
    @param:Schema(description = "이름으로 검색", example = "My Application")
    val name: String? = null,
    @param:Schema(description = "ID로 검색", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val id: String? = null,
    @field:Min(0)
    @param:Schema(description = "페이지 번호", example = "0", defaultValue = "0")
    val page: Int = 0,
    @field:Min(1)
    @param:Schema(description = "페이지 크기", example = "100", defaultValue = "100")
    val size: Int = 100,
)
