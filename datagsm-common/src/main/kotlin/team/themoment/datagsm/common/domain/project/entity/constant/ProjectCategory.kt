package team.themoment.datagsm.common.domain.project.entity.constant

import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
enum class ProjectCategory(
    val value: String,
) {
    PERSONAL("개인 프로젝트"),
    TEAM("팀 프로젝트"),
    CLUB("동아리 프로젝트"),
    IDEA_FESTIVAL("아이디어페스티벌"),
}
