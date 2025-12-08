package team.themoment.datagsm.domain.account.entity.constant

import org.springframework.security.core.GrantedAuthority
import team.themoment.datagsm.domain.student.entity.constant.StudentRole

/**
 * 인증/인가와 관련된 계정의 역할을 정의하는 열거형 클래스입니다.
 * @see StudentRole
 */
enum class AccountRole : GrantedAuthority {
    ROOT,
    ADMIN,
    USER,
    API_KEY_USER,
    ;

    /**
     * 권한 문자열을 "ROLE_" 접두어와 함께 반환합니다.
     */
    override fun getAuthority(): String = "ROLE_$name"
}
