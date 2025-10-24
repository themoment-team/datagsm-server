package team.themoment.datagsm.global.thirdparty.feign.oauth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleUserInfoResDto(
    @param:JsonProperty("id")
    val id: String,
    @param:JsonProperty("email")
    val email: String,
    @param:JsonProperty("verified_email")
    val verifiedEmail: Boolean,
    @param:JsonProperty("name")
    val name: String? = null,
    @param:JsonProperty("given_name")
    val givenName: String? = null,
    @param:JsonProperty("family_name")
    val familyName: String? = null,
    @param:JsonProperty("picture")
    val picture: String? = null,
    @param:JsonProperty("locale")
    val locale: String? = null,
)
