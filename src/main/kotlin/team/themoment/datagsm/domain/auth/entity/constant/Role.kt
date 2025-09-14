package team.themoment.datagsm.domain.auth.entity.constant

import org.springframework.security.core.GrantedAuthority

enum class Role : GrantedAuthority {
    STUDENT_COUNCIL,
    MEDIA_DEPARTMENT,
    DORMITORY_MANAGER,
    LIBRARY_MANAGER,
    GENERAL_STUDENT,
    TEACHER,
    ADMIN;

    override fun getAuthority(): String {
        return "ROLE_$name"
    }
}
