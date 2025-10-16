package team.themoment.datagsm.global.thirdparty.feign.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleUserInfoResDto(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("email")
    val email: String,

    @JsonProperty("verified_email")
    val verifiedEmail: Boolean,

    @JsonProperty("name")
    val name: String? = null,

    @JsonProperty("given_name")
    val givenName: String? = null,

    @JsonProperty("family_name")
    val familyName: String? = null,

    @JsonProperty("picture")
    val picture: String? = null,

    @JsonProperty("locale")
    val locale: String? = null
)
