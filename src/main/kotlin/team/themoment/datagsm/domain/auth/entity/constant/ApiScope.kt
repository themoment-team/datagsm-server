package team.themoment.datagsm.domain.auth.entity.constant

enum class ApiScope(
    val scope: String,
    val description: String,
) {
    // Student scopes
    STUDENT_ALL("student:*", "학생 정보 모든 권한"),
    STUDENT_READ("student:read", "학생 정보 조회"),
    STUDENT_WRITE("student:write", "학생 정보 생성/수정/삭제"),

    // Club scopes
    CLUB_ALL("club:*", "동아리 정보 모든 권한"),
    CLUB_READ("club:read", "동아리 정보 조회"),
    CLUB_WRITE("club:write", "동아리 정보 생성/수정/삭제"),

    // Project scopes
    PROJECT_ALL("project:*", "프로젝트 정보 모든 권한"),
    PROJECT_READ("project:read", "프로젝트 정보 조회"),
    PROJECT_WRITE("project:write", "프로젝트 정보 생성/수정/삭제"),
    ;

    companion object {
        private val ALL_SCOPES by lazy { entries.map { it.scope }.toSet() }

        fun fromString(scope: String): ApiScope? = entries.find { it.scope == scope }

        fun getAllScopes(): Set<String> = ALL_SCOPES
    }
}
