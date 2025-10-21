package team.themoment.datagsm.domain.auth.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.dto.ApiKeyResDto
import team.themoment.datagsm.domain.auth.dto.OAuthCodeReqDto
import team.themoment.datagsm.domain.auth.dto.RefreshTokenReqDto
import team.themoment.datagsm.domain.auth.dto.TokenResDto
import team.themoment.datagsm.domain.auth.service.AuthenticateGoogleOAuthService
import team.themoment.datagsm.domain.auth.service.CreateApiKeyService
import team.themoment.datagsm.domain.auth.service.DeleteApiKeyService
import team.themoment.datagsm.domain.auth.service.ReissueTokenService
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authenticateGoogleOAuthService: AuthenticateGoogleOAuthService,
    private val createApiKeyService: CreateApiKeyService,
    private val deleteApiKeyService: DeleteApiKeyService,
    private val reissueTokenService: ReissueTokenService,
) {
    @PostMapping("/google")
    fun authenticateWithGoogle(
        @RequestBody @Valid reqDto: OAuthCodeReqDto,
    ): TokenResDto = authenticateGoogleOAuthService.execute(reqDto.code)

    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody @Valid reqDto: RefreshTokenReqDto,
    ): TokenResDto = reissueTokenService.execute(reqDto.refreshToken)

    @PostMapping("/api-key")
    fun createApiKey(): ApiKeyResDto = createApiKeyService.execute()

    @DeleteMapping("/api-key")
    fun deleteApiKey(): CommonApiResponse<Nothing> {
        deleteApiKeyService.execute()
        return CommonApiResponse.success("API 키가 삭제되었습니다.")
    }
}
