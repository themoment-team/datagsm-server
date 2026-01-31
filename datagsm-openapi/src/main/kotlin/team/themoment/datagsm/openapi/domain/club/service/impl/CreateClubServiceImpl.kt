package team.themoment.datagsm.openapi.domain.club.service.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.common.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.openapi.domain.club.service.CreateClubService
import team.themoment.sdk.exception.ExpectedException

@Service
class CreateClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : CreateClubService {
    @Transactional
    override fun execute(clubReqDto: ClubReqDto): ClubResDto {
        if (clubJpaRepository.existsByName(clubReqDto.name)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다: ${clubReqDto.name}", HttpStatus.CONFLICT)
        }

        val leader =
            studentJpaRepository
                .findByIdOrNull(clubReqDto.leaderId)
                ?: throw ExpectedException(
                    "부장으로 지정한 학생을 찾을 수 없습니다. studentId: ${clubReqDto.leaderId}",
                    HttpStatus.NOT_FOUND,
                )

        val clubEntity =
            ClubJpaEntity().apply {
                name = clubReqDto.name
                type = clubReqDto.type
                this.leader = leader
            }
        val savedClubEntity = clubJpaRepository.save(clubEntity)

        return ClubResDto(
            id = savedClubEntity.id!!,
            name = savedClubEntity.name,
            type = savedClubEntity.type,
            leader =
                ParticipantInfoDto(
                    id = leader.id!!,
                    name = leader.name,
                    email = leader.email,
                    studentNumber = leader.studentNumber?.fullStudentNumber,
                    major = leader.major,
                    sex = leader.sex,
                ),
            participants = emptyList(),
        )
    }
}
