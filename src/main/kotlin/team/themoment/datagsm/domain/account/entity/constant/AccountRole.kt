package team.themoment.datagsm.domain.account.entity.constant

import org.springframework.security.core.GrantedAuthority

enum class AccountRole : GrantedAuthority {
    ROOT,
    ADMIN,
    USER,
    ;

    override fun getAuthority(): String = "ROLE_$name"
}
