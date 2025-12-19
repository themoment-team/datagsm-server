package team.themoment.datagsm.domain.auth.entity.constant

import team.themoment.datagsm.domain.account.entity.constant.AccountRole

enum class ApiScopeCategory(
    val displayName: String,
) {
    STUDENT("학생"),
    CLUB("동아리"),
    PROJECT("프로젝트"),
    NEIS("NEIS"),
    ADMIN("관리자"),
    ;

    companion object {
        fun fromScope(scope: String): ApiScopeCategory? {
            val category = scope.substringBefore(':')
            return entries.find { it.name.equals(category, ignoreCase = true) }
        }
    }
}

enum class ApiScope(
    val scope: String,
    val description: String,
    val accountRole: AccountRole?,
    val category: ApiScopeCategory?,
) {
    // Auth scopes
    AUTH_MANAGE("auth:manage", "API 키 관리 (생성/조회/수정/삭제)", null, null),

    // Admin scopes
    ADMIN_ALL("admin:*", "관리자 모든 권한", AccountRole.ADMIN, ApiScopeCategory.ADMIN),
    ADMIN_APIKEY("admin:apikey", "Admin API 키 생성/갱신", AccountRole.ADMIN, ApiScopeCategory.ADMIN),
    ADMIN_EXCEL("admin:excel", "Excel 파일 업로드/다운로드", AccountRole.ADMIN, ApiScopeCategory.ADMIN),

    // Student scopes
    STUDENT_ALL("student:*", "학생 정보 모든 권한", AccountRole.ADMIN, ApiScopeCategory.STUDENT),
    STUDENT_READ("student:read", "학생 정보 조회", AccountRole.USER, ApiScopeCategory.STUDENT),
    STUDENT_WRITE("student:write", "학생 정보 생성/수정/삭제", AccountRole.ADMIN, ApiScopeCategory.STUDENT),

    // Club scopes
    CLUB_ALL("club:*", "동아리 정보 모든 권한", AccountRole.ADMIN, ApiScopeCategory.CLUB),
    CLUB_READ("club:read", "동아리 정보 조회", AccountRole.USER, ApiScopeCategory.CLUB),
    CLUB_WRITE("club:write", "동아리 정보 생성/수정/삭제", AccountRole.ADMIN, ApiScopeCategory.CLUB),

    // Project scopes
    PROJECT_ALL("project:*", "프로젝트 정보 모든 권한", AccountRole.ADMIN, ApiScopeCategory.PROJECT),
    PROJECT_READ("project:read", "프로젝트 정보 조회", AccountRole.USER, ApiScopeCategory.PROJECT),
    PROJECT_WRITE("project:write", "프로젝트 정보 생성/수정/삭제", AccountRole.ADMIN, ApiScopeCategory.PROJECT),

    // NEIS scopes
    NEIS_ALL("neis:*", "NEIS 정보 모든 권한", AccountRole.ADMIN, ApiScopeCategory.NEIS),
    NEIS_READ("neis:read", "NEIS 정보 조회 (학사일정/급식)", AccountRole.USER, ApiScopeCategory.NEIS),
    ;

    companion object {
        const val ALL_SCOPE = "*:*"

        private val ALL_SCOPES by lazy { entries.map { it.scope }.toSet() }

        val READ_ONLY_SCOPES =
            setOf(
                STUDENT_READ.scope,
                CLUB_READ.scope,
                PROJECT_READ.scope,
                NEIS_READ.scope,
            )

        fun fromString(scope: String): ApiScope? = entries.find { it.scope == scope }

        fun getAllScopes(): Set<String> = ALL_SCOPES
    }
}
