package team.themoment.datagsm.common.domain.event.mapper

import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.event.dto.payload.ClubEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.EventClubRef
import team.themoment.datagsm.common.domain.event.dto.payload.EventStudentRef
import team.themoment.datagsm.common.domain.event.dto.payload.ProjectEventObject
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventObject
import team.themoment.datagsm.common.domain.project.entity.ProjectJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity

/**
 * 엔티티를 webhook payload 스냅샷으로 변환한다.
 * 연관 엔티티를 순회하므로 반드시 트랜잭션 안에서 호출해야 한다.
 */
object EventObjectMapper {
    fun from(student: StudentJpaEntity): StudentEventObject =
        StudentEventObject(
            studentId = student.id!!,
            name = student.name,
            email = student.email,
            sex = student.sex.name,
            grade = student.studentNumber?.studentGrade,
            classNum = student.studentNumber?.studentClass,
            number = student.studentNumber?.studentNumber,
            studentNumber = student.studentNumber?.fullStudentNumber,
            major = student.major?.name,
            specialty = student.specialty,
            role = student.role.name,
            dormitoryFloor = student.dormitoryRoomNumber?.dormitoryRoomFloor,
            dormitoryRoom = student.dormitoryRoomNumber?.dormitoryRoomNumber,
            majorClubName = student.majorClub?.name,
            autonomousClubName = student.autonomousClub?.name,
            githubId = student.githubId,
        )

    /**
     * 동아리 소속 학생은 [ClubJpaEntity] 에서 역참조할 수 없어 호출자가 조회한 값을 받는다.
     */
    fun from(
        club: ClubJpaEntity,
        leader: StudentJpaEntity?,
        participants: List<StudentJpaEntity>,
    ): ClubEventObject =
        ClubEventObject(
            clubId = club.id!!,
            name = club.name,
            type = club.type.name,
            foundedYear = club.foundedYear,
            status = club.status.name,
            abolishedYear = club.abolishedYear,
            leader = leader?.let { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
            participants = participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
        )

    fun from(project: ProjectJpaEntity): ProjectEventObject =
        ProjectEventObject(
            projectId = project.id!!,
            name = project.name,
            description = project.description,
            startYear = project.startYear,
            endYear = project.endYear,
            status = project.status.name,
            club = project.club?.let { EventClubRef(it.id!!, it.name) },
            participants =
                project.participants.map { EventStudentRef(it.studentNumber?.fullStudentNumber, it.name) },
        )
}
