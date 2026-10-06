package team.themoment.datagsm.common.domain.project.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.ksp.annotation.SdkExport

/** 공개 API용 참여자 정보 — 이메일 등 개인 식별 정보는 노출하지 않는다 */
@SdkExport
data class PublicParticipantInfoDto(
    @field:Schema(description = "학생 이름", example = "홍길동")
    val name: String,
    @field:Schema(description = "학과", example = "SW_DEVELOPMENT")
    val major: Major?,
)
