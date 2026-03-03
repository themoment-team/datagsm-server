package team.themoment.datagsm.common.domain.oauth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class OauthSessionResDto(
    @field:JsonProperty("service_name")
    @field:Schema(description = "서비스 이름")
    val serviceName: String,
)
