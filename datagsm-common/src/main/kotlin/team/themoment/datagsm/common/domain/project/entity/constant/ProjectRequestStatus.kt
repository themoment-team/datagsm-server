package team.themoment.datagsm.common.domain.project.entity.constant

import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
enum class ProjectRequestStatus(
    val value: String,
) {
    PENDING("신청 대기 중"),
    ACCEPTED("수락"),
    REJECTED("거절"),
}
