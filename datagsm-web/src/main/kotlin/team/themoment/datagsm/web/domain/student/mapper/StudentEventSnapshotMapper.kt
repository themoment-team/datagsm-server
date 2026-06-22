package team.themoment.datagsm.web.domain.student.mapper

import org.springframework.stereotype.Component
import team.themoment.datagsm.common.domain.event.dto.payload.StudentEventSnapshot
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity

@Component
class StudentEventSnapshotMapper {
    fun toSnapshot(
        index: Int,
        student: StudentJpaEntity,
    ): StudentEventSnapshot =
        StudentEventSnapshot(
            index = index,
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

    fun toSnapshots(students: List<StudentJpaEntity>): List<StudentEventSnapshot> =
        students.mapIndexed { index, student -> toSnapshot(index, student) }
}
