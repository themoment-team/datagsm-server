package team.themoment.datagsm.common.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.GlobalOpenApiCustomizer

class SecurityRequirementCustomizer(
    private val schemeName: String,
    private val securityScheme: SecurityScheme,
    private val isPublicPath: (String) -> Boolean,
) : GlobalOpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        val components = openApi.components ?: Components().also { openApi.components = it }
        components.addSecuritySchemes(schemeName, securityScheme)
        openApi.addSecurityItem(SecurityRequirement().addList(schemeName))

        // 빈 목록은 OpenAPI에서 전역 requirement를 해제한다는 뜻이다.
        openApi.paths
            ?.filterKeys(isPublicPath)
            ?.values
            ?.flatMap { it.readOperations() }
            ?.forEach { it.security = emptyList() }
    }
}
