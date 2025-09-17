package team.themoment.datagsm.domain.project.dto.internal

import team.themoment.datagsm.domain.club.dto.internal.ClubDto

data class ProjectDto(
    val projectId: Long,
    val projectName: String,
    val projectDescription: String,
    val projectOwnerClub: ClubDto,
)
