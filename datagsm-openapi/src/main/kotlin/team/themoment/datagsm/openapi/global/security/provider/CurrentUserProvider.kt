package team.themoment.datagsm.openapi.global.security.provider

import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import team.themoment.datagsm.openapi.global.security.authentication.ApiKeyAuthenticationToken
import team.themoment.datagsm.openapi.global.security.authentication.principal.ApiKeyPrincipal
import team.themoment.sdk.exception.ExpectedException

@Component
class CurrentUserProvider {
    fun getAuthentication(): ApiKeyAuthenticationToken {
        val authentication: Authentication? =
            SecurityContextHolder
                .getContext()
                .authentication
        if (authentication == null) {
            throw ExpectedException("인증 정보가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        if (authentication !is ApiKeyAuthenticationToken) {
            throw ExpectedException("인증 정보가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED)
        }
        return authentication
    }

    fun getPrincipal(): ApiKeyPrincipal = getAuthentication().principal
}
