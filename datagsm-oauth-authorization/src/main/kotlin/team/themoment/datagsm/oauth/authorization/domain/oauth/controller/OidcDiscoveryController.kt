package team.themoment.datagsm.oauth.authorization.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.themoment.datagsm.common.domain.oauth.dto.response.OidcDiscoveryResDto
import team.themoment.datagsm.oauth.authorization.domain.oauth.service.QueryOidcDiscoveryService

/**
 * OIDC Discovery는 경로가 규격으로 고정되어 있어 /v1 하위에 둘 수 없다.
 * CommonApiResponse 래핑도 하지 않는다 (application.yml의 not-wrapping-urls 참고).
 */
@Tag(name = "OIDC", description = "OpenID Connect Discovery")
@RestController
class OidcDiscoveryController(
    val queryOidcDiscoveryService: QueryOidcDiscoveryService,
) {
    @GetMapping("/.well-known/openid-configuration")
    @Operation(
        summary = "OIDC Discovery 문서 조회",
        description = "SP가 issuer만 설정하면 나머지 엔드포인트를 자동으로 찾을 수 있도록 메타데이터를 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ],
    )
    fun getOidcDiscovery(): OidcDiscoveryResDto = queryOidcDiscoveryService.execute()
}
