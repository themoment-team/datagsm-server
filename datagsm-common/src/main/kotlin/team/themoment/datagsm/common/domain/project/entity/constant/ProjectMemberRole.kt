package team.themoment.datagsm.common.domain.project.entity.constant

import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
enum class ProjectMemberRole(
    val value: String,
) {
    OWNER("신청자"),
    PARTICIPANT("참여자"),
}
