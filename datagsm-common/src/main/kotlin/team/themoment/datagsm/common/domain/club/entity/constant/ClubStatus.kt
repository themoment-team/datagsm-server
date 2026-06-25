package team.themoment.datagsm.common.domain.club.entity.constant

import team.themoment.datagsm.ksp.annotation.SdkExport

@SdkExport
enum class ClubStatus(
    val value: String,
) {
    ACTIVE("운영 중"),
    ABOLISHED("폐지"),
}
