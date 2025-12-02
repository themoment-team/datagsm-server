package team.themoment.datagsm.domain.project.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.project.dto.request.ProjectReqDto
import team.themoment.datagsm.domain.project.dto.response.ProjectResDto
import team.themoment.datagsm.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.domain.project.repository.ProjectJpaRepository
import team.themoment.datagsm.domain.project.service.CreateProjectService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class CreateProjectServiceImpl(
    private val projectJpaRepository: ProjectJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
) : CreateProjectService {
    override fun execute(projectReqDto: ProjectReqDto): ProjectResDto {
        if (projectJpaRepository.existsByName(projectReqDto.name)) {
            throw ExpectedException("이미 존재하는 프로젝트 이름입니다: ${projectReqDto.name}", HttpStatus.CONFLICT)
        }

        val ownerClub =
            clubJpaRepository
                .findById(projectReqDto.clubId)
                .orElseThrow {
                    ExpectedException(
                        "동아리를 찾을 수 없습니다. clubId: ${projectReqDto.clubId}",
                        HttpStatus.NOT_FOUND,
                    )
                }

        val projectEntity =
            ProjectJpaEntity().apply {
                name = projectReqDto.name
                description = projectReqDto.description
                this.club = ownerClub
            }
        val savedProjectEntity = projectJpaRepository.save(projectEntity)

        return ProjectResDto(
            id = savedProjectEntity.id!!,
            name = savedProjectEntity.name,
            description = savedProjectEntity.description,
            club =
                ClubResDto(
                    id = ownerClub.id!!,
                    name = ownerClub.name,
                    type = ownerClub.type,
                ),
        )
    }
}
