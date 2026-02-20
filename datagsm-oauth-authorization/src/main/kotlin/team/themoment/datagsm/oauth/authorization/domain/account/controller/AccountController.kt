package team.themoment.datagsm.oauth.authorization.domain.account.controller

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
import team.themoment.datagsm.common.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.common.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.common.domain.account.dto.request.SendEmailReqDto
import team.themoment.datagsm.common.domain.account.dto.request.SendPasswordResetEmailReqDto
import team.themoment.datagsm.common.domain.account.dto.request.VerifyPasswordResetCodeReqDto
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckPasswordResetCodeService
import team.themoment.datagsm.oauth.authorization.domain.account.service.CheckSignupEmailService
import team.themoment.datagsm.oauth.authorization.domain.account.service.CreateAccountService
import team.themoment.datagsm.oauth.authorization.domain.account.service.ModifyPasswordService
import team.themoment.datagsm.oauth.authorization.domain.account.service.SendPasswordResetEmailService
import team.themoment.datagsm.oauth.authorization.domain.account.service.SendSignupEmailService

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequestMapping("/v1/accounts")
class AccountController(
    private val sendPasswordResetEmailService: SendPasswordResetEmailService,
    private val checkPasswordResetCodeService: CheckPasswordResetCodeService,
    private val modifyPasswordService: ModifyPasswordService,
    private val sendSignupEmailService: SendSignupEmailService,
    private val checkSignupEmailService: CheckSignupEmailService,
    private val createAccountService: CreateAccountService,
) {
    @Operation(summary = "비밀번호 재설정 이메일 발송", description = "비밀번호 재설정을 위한 인증 코드를 이메일로 발송합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 이메일", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 횟수 초과", content = [Content()]),
        ],
    )
    @PostMapping("/password-resets")
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
    @PostMapping("/password-resets/verification")
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
    @PutMapping("/password")
    fun modifyPassword(
        @RequestBody @Valid reqDto: ChangePasswordReqDto,
    ) {
        modifyPasswordService.execute(reqDto)
    }

    @Operation(summary = "회원가입 인증 코드 발송", description = "회원가입을 위한 이메일 인증 코드를 발송합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 횟수 초과", content = [Content()]),
        ],
    )
    @PostMapping("/email-verifications")
    fun sendSignupEmail(
        @RequestBody @Valid reqDto: SendEmailReqDto,
    ) {
        sendSignupEmailService.execute(reqDto)
    }

    @Operation(summary = "회원가입 인증 코드 검증", description = "이메일로 받은 회원가입 인증 코드를 검증합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "코드 검증 성공"),
            ApiResponse(responseCode = "400", description = "인증 코드 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드가 존재하지 않음", content = [Content()]),
            ApiResponse(responseCode = "429", description = "요청 횟수 초과", content = [Content()]),
        ],
    )
    @PostMapping("/email-verifications/verify")
    fun verifySignupEmail(
        @RequestBody @Valid reqDto: EmailCodeReqDto,
    ) {
        checkSignupEmailService.execute(reqDto)
    }

    @Operation(summary = "계정 생성", description = "검증된 이메일로 새로운 계정을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "계정 생성 성공"),
            ApiResponse(responseCode = "400", description = "인증 코드 불일치", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드가 존재하지 않음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = [Content()]),
        ],
    )
    @PostMapping
    fun createAccount(
        @RequestBody @Valid reqDto: CreateAccountReqDto,
    ) {
        createAccountService.execute(reqDto)
    }
}
