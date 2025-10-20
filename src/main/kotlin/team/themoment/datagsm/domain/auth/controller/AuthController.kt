package team.themoment.datagsm.domain.auth.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.dto.ApiKeyResDto
import team.themoment.datagsm.domain.auth.dto.OAuthCodeReqDto
import team.themoment.datagsm.domain.auth.dto.TokenResDto
import team.themoment.datagsm.domain.auth.service.AuthenticateGoogleOAuthService
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.domain.auth.service.DeleteApiKeyService

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authenticateGoogleOAuthService: AuthenticateGoogleOAuthService,
    private val createApiKeyService: CreateApiKeyService,
    private val deleteApiKeyService: DeleteApiKeyService,
) {
    @PostMapping
    fun authenticateWithGoogle(
        @RequestBody @Valid reqDto: OAuthCodeReqDto,
    ): TokenResDto = authenticateGoogleOAuthService.execute(reqDto.code)

    @PostMapping("/api-key")
    fun createApiKey(
        @RequestHeader("Authorization") authorization: String,
    ): ApiKeyResDto = createApiKeyService.execute(authorization)

    @DeleteMapping("/api-key")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteApiKey(
        @RequestHeader("Authorization") authorization: String,
    ) {
        deleteApiKeyService.execute(authorization)
    }
}
