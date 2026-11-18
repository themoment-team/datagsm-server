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
    @Operation(
        summary = "비밀번호 재설정 이메일 발송",
        description = "비밀번호 재설정을 위한 인증 코드를 이메일로 발송합니다. 계정이 없는 이메일도 존재 여부를 노출하지 않고 동일하게 204를 반환하며 메일은 발송하지 않습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "인증 코드 발송 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 검증 실패 (이메일 형식 또는 길이)", content = [Content()]),
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
            ApiResponse(responseCode = "204", description = "코드 검증 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 검증 실패 / 인증 코드 불일치", content = [Content()]),
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

    @Operation(summary = "비밀번호 변경", description = "검증된 인증 코드로 비밀번호를 변경합니다. 모든 Refresh Token과 IdP 세션이 무효화되고 사용한 재설정 코드가 삭제됩니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "비밀번호 변경 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 검증 실패 / 인증 코드 미검증 / 코드 불일치 / 이전 비밀번호와 동일", content = [Content()]),
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

    @Operation(summary = "회원가입 인증 코드 발송", description = "gsm.hs.kr 도메인 이메일에 회원가입 인증 코드를 발송합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "인증 코드 발송 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 검증 실패 / gsm.hs.kr 도메인이 아닌 이메일", content = [Content()]),
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
            ApiResponse(responseCode = "204", description = "코드 검증 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 검증 실패 / 인증 코드 불일치", content = [Content()]),
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

    @Operation(
        summary = "계정 생성",
        description = "이메일 인증 코드를 확인하고 소비하여 새로운 계정을 생성합니다. 학생 계정은 ACTIVE, 선생님 계정은 관리자 승인 대기인 PENDING으로 생성됩니다. 선생님 가입 시 이름과 소속 부서가 필수입니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "계정 생성 성공", content = [Content()]),
            ApiResponse(responseCode = "400", description = "요청 검증 실패 / 인증 코드 불일치 / 선생님 이름 또는 소속 부서 누락", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드가 존재하지 않음", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 계정 또는 선생님 정보가 존재하는 이메일", content = [Content()]),
            ApiResponse(
                responseCode = "429",
                description = "요청 횟수 초과 (회원가입 인증 코드 검증 API와 동일한 한도를 공유합니다)",
                content = [Content()],
            ),
        ],
    )
    @PostMapping
    fun createAccount(
        @RequestBody @Valid reqDto: CreateAccountReqDto,
    ) {
        createAccountService.execute(reqDto)
    }
}
