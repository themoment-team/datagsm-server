package team.themoment.datagsm.domain.auth.entity.constant

import team.themoment.datagsm.domain.account.entity.constant.AccountRole

enum class ApiScope(
    val scope: String,
    val description: String,
    val accountRole: AccountRole?,
) {
    // Auth scopes
    AUTH_MANAGE("auth:manage", "API 키 관리 (생성/조회/수정/삭제)", null),

    // Admin scopes
    ADMIN_ALL("admin:*", "관리자 모든 권한", AccountRole.ADMIN),
    ADMIN_APIKEY("admin:apikey", "Admin API 키 생성/갱신", AccountRole.ADMIN),
    ADMIN_EXCEL("admin:excel", "Excel 파일 업로드/다운로드", AccountRole.ADMIN),

    // Student scopes
    STUDENT_ALL("student:*", "학생 정보 모든 권한", AccountRole.ADMIN),
    STUDENT_READ("student:read", "학생 정보 조회", AccountRole.USER),
    STUDENT_WRITE("student:write", "학생 정보 생성/수정/삭제", AccountRole.ADMIN),

    // Club scopes
    CLUB_ALL("club:*", "동아리 정보 모든 권한", AccountRole.ADMIN),
    CLUB_READ("club:read", "동아리 정보 조회", AccountRole.USER),
    CLUB_WRITE("club:write", "동아리 정보 생성/수정/삭제", AccountRole.ADMIN),

    // Project scopes
    PROJECT_ALL("project:*", "프로젝트 정보 모든 권한", AccountRole.ADMIN),
    PROJECT_READ("project:read", "프로젝트 정보 조회", AccountRole.USER),
    PROJECT_WRITE("project:write", "프로젝트 정보 생성/수정/삭제", AccountRole.ADMIN),

    // NEIS scopes
    NEIS_ALL("neis:*", "NEIS 정보 모든 권한", AccountRole.ADMIN),
    NEIS_READ("neis:read", "NEIS 정보 조회 (학사일정/급식)", AccountRole.USER),
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
