package team.themoment.datagsm.domain.auth.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.auth.dto.OAuthCodeReqDto
import team.themoment.datagsm.domain.auth.dto.TokenResDto
import team.themoment.datagsm.domain.auth.service.AuthenticateGoogleOAuthService

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val authenticateGoogleOAuthService: AuthenticateGoogleOAuthService,
) {
    @PostMapping
    fun authenticateWithGoogle(
        @RequestBody @Valid reqDto: OAuthCodeReqDto,
    ): TokenResDto = authenticateGoogleOAuthService.execute(reqDto.code)
}
