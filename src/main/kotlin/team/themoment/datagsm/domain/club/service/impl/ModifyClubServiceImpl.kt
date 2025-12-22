package team.themoment.datagsm.domain.club.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.club.dto.internal.ParticipantInfoDto
import team.themoment.datagsm.domain.club.dto.request.ClubReqDto
import team.themoment.datagsm.domain.club.dto.response.ClubResDto
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.ModifyClubService
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class ModifyClubServiceImpl(
    private val clubJpaRepository: ClubJpaRepository,
    private val studentJpaRepository: StudentJpaRepository,
) : ModifyClubService {
    override fun execute(
        clubId: Long,
        reqDto: ClubReqDto,
    ): ClubResDto {
        val club =
            clubJpaRepository
                .findById(clubId)
                .orElseThrow { ExpectedException("동아리를 찾을 수 없습니다. clubId: $clubId", HttpStatus.NOT_FOUND) }
        if (clubJpaRepository.existsByNameAndIdNot(reqDto.name, clubId)) {
            throw ExpectedException("이미 존재하는 동아리 이름입니다: ${reqDto.name}", HttpStatus.CONFLICT)
        }

        val newLeader =
            studentJpaRepository
                .findById(reqDto.leaderId)
                .orElseThrow {
                    ExpectedException("부장으로 지정한 학생을 찾을 수 없습니다. studentId: ${reqDto.leaderId}", HttpStatus.NOT_FOUND)
                }

        club.name = reqDto.name
        club.type = reqDto.type
        club.leader = newLeader

        val participants = getParticipantsByClubType(club)

        return ClubResDto(
            id = club.id!!,
            name = club.name,
            type = club.type,
            leader =
                ParticipantInfoDto(
                    id = newLeader.id!!,
                    name = newLeader.name,
                    email = newLeader.email,
                    studentNumber =
                        newLeader.studentNumber.studentGrade * 1000 + newLeader.studentNumber.studentClass * 100 +
                            newLeader.studentNumber.studentNumber,
                    major = newLeader.major,
                    sex = newLeader.sex,
                ),
            participants =
                participants
                    .filter { it.id != newLeader.id }
                    .map { student ->
                        ParticipantInfoDto(
                            id = student.id!!,
                            name = student.name,
                            email = student.email,
                            studentNumber =
                                student.studentNumber.studentGrade * 1000 + student.studentNumber.studentClass * 100 +
                                    student.studentNumber.studentNumber,
                            major = student.major,
                            sex = student.sex,
                        )
                    },
        )
    }

    private fun getParticipantsByClubType(club: team.themoment.datagsm.domain.club.entity.ClubJpaEntity): List<StudentJpaEntity> =
        when (club.type) {
            ClubType.MAJOR_CLUB -> studentJpaRepository.findByMajorClub(club)
            ClubType.JOB_CLUB -> studentJpaRepository.findByJobClub(club)
            ClubType.AUTONOMOUS_CLUB -> studentJpaRepository.findByAutonomousClub(club)
        }
}
