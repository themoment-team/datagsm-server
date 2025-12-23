package team.themoment.datagsm.domain.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.domain.account.dto.request.CreateAccountReqDto
import team.themoment.datagsm.domain.account.dto.request.EmailCodeReqDto
import team.themoment.datagsm.domain.account.dto.request.SendEmailReqDto
import team.themoment.datagsm.domain.account.service.CheckEmailService
import team.themoment.datagsm.domain.account.service.CreateAccountService
import team.themoment.datagsm.domain.account.service.SendEmailService
import team.themoment.datagsm.global.common.response.dto.response.CommonApiResponse

@Tag(name = "Account", description = "계정 관련 API")
@RestController
@RequestMapping("/v1/account")
class AccountController(
    val sendEmailService: SendEmailService,
    val checkEmailService: CheckEmailService,
    val createAccountService: CreateAccountService,
) {
    @Operation(summary = "인증 메일 전송", description = "이메일 인증을 위한 코드를 전송합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 메일 전송 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
        ],
    )
    @PostMapping("/email/send")
    fun sendEmail(
        @RequestBody @Valid reqDto: SendEmailReqDto,
    ): CommonApiResponse<Nothing> {
        sendEmailService.execute(reqDto)
        return CommonApiResponse.success("인증 메일이 ${reqDto.email}로 전송되었습니다.")
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 인증 코드가 유효한지 확인합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "인증 코드 확인 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드를 찾을 수 없음 / 코드 불일치", content = [Content()]),
        ],
    )
    @GetMapping("/email/check")
    fun checkEmail(
        @Valid reqDto: EmailCodeReqDto,
    ): CommonApiResponse<Nothing> {
        checkEmailService.execute(reqDto)
        return CommonApiResponse.success("유효한 코드 입니다.")
    }

    @Operation(summary = "회원가입", description = "이메일 인증 후 계정을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (검증 실패)", content = [Content()]),
            ApiResponse(responseCode = "404", description = "인증 코드를 찾을 수 없음 / 코드 불일치", content = [Content()]),
            ApiResponse(responseCode = "409", description = "이미 존재하는 계정", content = [Content()]),
        ],
    )
    @PostMapping("/signup")
    fun signUp(
        @RequestBody @Valid reqDto: CreateAccountReqDto,
    ): CommonApiResponse<Nothing> {
        createAccountService.execute(reqDto)
        return CommonApiResponse.success("회원가입이 완료되었습니다.")
    }
}
