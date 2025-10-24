package team.themoment.datagsm.domain.project.dto.internal

import team.themoment.datagsm.domain.club.dto.response.ClubResDto

data class ProjectDto(
    val projectId: Long,
    val projectName: String,
    val projectDescription: String,
    val projectOwnerClub: ClubResDto,
)
