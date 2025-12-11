package team.themoment.datagsm.domain.auth.entity.constant

enum class ApiScope(
    val scope: String,
    val description: String,
) {
    // Auth scopes
    AUTH_MANAGE("auth:manage", "API 키 관리 (생성/조회/수정/삭제)"),

    // Admin scopes
    ADMIN_ALL("admin:*", "관리자 모든 권한"),
    ADMIN_EXCEL("admin:excel", "Excel 파일 업로드/다운로드"),

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
        const val ALL_SCOPE = "*:*"

        private val ALL_SCOPES by lazy { entries.map { it.scope }.toSet() }

        fun fromString(scope: String): ApiScope? = entries.find { it.scope == scope }

        fun getAllScopes(): Set<String> = ALL_SCOPES
    }
}
