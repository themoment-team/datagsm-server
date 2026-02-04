package team.themoment.datagsm.oauth.authorization.domain.password.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.account.dto.request.ChangePasswordReqDto
import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto
import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto
import team.themoment.datagsm.oauth.authorization.domain.password.service.CheckPasswordResetCodeService
import team.themoment.datagsm.oauth.authorization.domain.password.service.ModifyPasswordService
import team.themoment.datagsm.oauth.authorization.domain.password.service.SendPasswordResetEmailService

@Tag(name = "Password", description = "비밀번호 재설정 API")
@RestController
@RequestMapping("/v1/password")
class PasswordController(
    private val sendPasswordResetEmailService: SendPasswordResetEmailService,
    private val checkPasswordResetCodeService: CheckPasswordResetCodeService,
    private val modifyPasswordService: ModifyPasswordService,
) {
    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정을 위한 인증 코드를 이메일로 발송합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 이메일", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 횟수 초과", content = [Content()]),
        ],
    )
    @PostMapping("/reset/send")
    fun sendPasswordResetEmail(
        @RequestBody @Valid reqDto: SendPasswordResetEmailReqDto,
    ) {
        sendPasswordResetEmailService.execute(reqDto)
    }

    @Operation(summary = "비밀번호 재설정 코드 검증", description = "이메일로 받은 인증 코드를 검증합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "코드 검증 성공"),
            ApiResponse(responseCode = "400", description = "인증 코드 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드가 존재하지 않음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 횟수 초과", content = [Content()]),
        ],
    )
    @PostMapping("/reset/verify")
    fun checkPasswordResetCode(
        @RequestBody @Valid reqDto: VerifyPasswordResetCodeReqDto,
    ) {
        checkPasswordResetCodeService.execute(reqDto)
    }

    @Operation(summary = "비밀번호 변경", description = "검증된 인증 코드로 비밀번호를 변경합니다. 모든 Refresh Token이 무효화됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            ApiResponse(responseCode = "400", description = "인증 코드 미검증 / 코드 불일치 / 이전 비밀번호와 동일", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드 또는 계정이 존재하지 않음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 횟수 초과", content = [Content()]),
        ],
    )
    @PutMapping("/change")
    fun modifyPassword(
        @RequestBody @Valid reqDto: ChangePasswordReqDto,
    ) {
        modifyPasswordService.execute(reqDto)
    }
}
