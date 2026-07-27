package team.themoment.datagsm.common.domain.project.entity.constant

import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
enum class ProjectStatus(
    val value: String,
) {
    ACTIVE("운영 중"),
    ENDED("종료"),
}
