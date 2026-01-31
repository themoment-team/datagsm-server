package team.themoment.datagsm.common.domain.student.repository.custom.impl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.QAccountJpaEntity.Companion.accountJpaEntity
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.QStudentJpaEntity.Companion.studentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.entity.constant.StudentSortBy
import team.themoment.datagsm.common.domain.student.repository.custom.StudentJpaCustomRepository
import team.themoment.datagsm.common.global.constant.SortDirection

@Repository
class StudentJpaCustomRepositoryImpl(
    val jpaQueryFactory: JPAQueryFactory,
) : StudentJpaCustomRepository {
    override fun searchStudentsWithPaging(
        id: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: StudentRole?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean?,
        includeGraduates: Boolean,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Page<StudentJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val content =
            jpaQueryFactory
                .selectFrom(studentJpaEntity)
                .where(
                    id?.let { studentJpaEntity.id.eq(it) },
                    name?.let { studentJpaEntity.name.contains(it) },
                    email?.let { studentJpaEntity.email.contains(it) },
                    grade?.let { studentJpaEntity.studentNumber.studentGrade.eq(it) },
                    classNum?.let { studentJpaEntity.studentNumber.studentClass.eq(it) },
                    number?.let { studentJpaEntity.studentNumber.studentNumber.eq(it) },
                    sex?.let { studentJpaEntity.sex.eq(it) },
                    role?.let { studentJpaEntity.role.eq(it) },
                    dormitoryRoom?.let { studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { studentJpaEntity.isLeaveSchool.eq(it) },
                    if (!includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(studentJpaEntity.count())
                .from(studentJpaEntity)
                .where(
                    id?.let { studentJpaEntity.id.eq(it) },
                    name?.let { studentJpaEntity.name.contains(it) },
                    email?.let { studentJpaEntity.email.contains(it) },
                    grade?.let { studentJpaEntity.studentNumber.studentGrade.eq(it) },
                    classNum?.let { studentJpaEntity.studentNumber.studentClass.eq(it) },
                    number?.let { studentJpaEntity.studentNumber.studentNumber.eq(it) },
                    sex?.let { studentJpaEntity.sex.eq(it) },
                    role?.let { studentJpaEntity.role.eq(it) },
                    dormitoryRoom?.let { studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { studentJpaEntity.isLeaveSchool.eq(it) },
                    if (!includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun createOrderSpecifier(
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Array<OrderSpecifier<*>>? {
        if (sortBy == null) return null

        return when (sortBy) {
            StudentSortBy.STUDENT_NUMBER -> {
                when (sortDirection) {
                    SortDirection.ASC -> {
                        arrayOf(
                            studentJpaEntity.studentNumber.studentGrade.asc(),
                            studentJpaEntity.studentNumber.studentClass.asc(),
                            studentJpaEntity.studentNumber.studentNumber.asc(),
                        )
                    }

                    SortDirection.DESC -> {
                        arrayOf(
                            studentJpaEntity.studentNumber.studentGrade.desc(),
                            studentJpaEntity.studentNumber.studentClass.desc(),
                            studentJpaEntity.studentNumber.studentNumber.desc(),
                        )
                    }
                }
            }

            StudentSortBy.DORMITORY_ROOM -> {
                val path = studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber
                arrayOf(
                    when (sortDirection) {
                        SortDirection.ASC -> path.asc().nullsLast()
                        SortDirection.DESC -> path.desc().nullsLast()
                    },
                )
            }

            else -> {
                val path =
                    when (sortBy) {
                        StudentSortBy.ID -> studentJpaEntity.id
                        StudentSortBy.NAME -> studentJpaEntity.name
                        StudentSortBy.EMAIL -> studentJpaEntity.email
                        StudentSortBy.GRADE -> studentJpaEntity.studentNumber.studentGrade
                        StudentSortBy.CLASS_NUM -> studentJpaEntity.studentNumber.studentClass
                        StudentSortBy.NUMBER -> studentJpaEntity.studentNumber.studentNumber
                        StudentSortBy.MAJOR -> studentJpaEntity.major
                        StudentSortBy.ROLE -> studentJpaEntity.role
                        StudentSortBy.SEX -> studentJpaEntity.sex
                        StudentSortBy.IS_LEAVE_SCHOOL -> studentJpaEntity.isLeaveSchool
                    }
                arrayOf(
                    when (sortDirection) {
                        SortDirection.ASC -> path.asc()
                        SortDirection.DESC -> path.desc()
                    },
                )
            }
        }
    }

    override fun existsByEmail(email: String): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(studentJpaEntity.email.eq(email))
            .fetchFirst() != null

    override fun existsByStudentNumber(
        grade: Int,
        classNum: Int,
        number: Int,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(
                studentJpaEntity.studentNumber.studentGrade
                    .eq(grade)
                    .and(studentJpaEntity.studentNumber.studentClass.eq(classNum))
                    .and(studentJpaEntity.studentNumber.studentNumber.eq(number)),
            ).fetchFirst() != null

    override fun existsByStudentEmailAndNotId(
        email: String,
        id: Long,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(
                studentJpaEntity.email
                    .eq(email)
                    .and(studentJpaEntity.id.ne(id)),
            ).fetchFirst() != null

    override fun existsByStudentNumberAndNotId(
        grade: Int,
        classNum: Int,
        number: Int,
        id: Long,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(
                studentJpaEntity.studentNumber.studentGrade
                    .eq(grade)
                    .and(studentJpaEntity.studentNumber.studentClass.eq(classNum))
                    .and(studentJpaEntity.studentNumber.studentNumber.eq(number))
                    .and(studentJpaEntity.id.ne(id)),
            ).fetchFirst() != null

    override fun findStudentsByGrade(grade: Int): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .leftJoin(studentJpaEntity.majorClub)
            .fetchJoin()
            .leftJoin(studentJpaEntity.jobClub)
            .fetchJoin()
            .leftJoin(studentJpaEntity.autonomousClub)
            .fetchJoin()
            .where(studentJpaEntity.studentNumber.studentGrade.eq(grade))
            .orderBy(
                studentJpaEntity.studentNumber.studentClass.asc(),
                studentJpaEntity.studentNumber.studentNumber.asc(),
            ).fetch()

    override fun findAllStudents(): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .orderBy(
                studentJpaEntity.studentNumber.studentGrade.asc(),
                studentJpaEntity.studentNumber.studentClass.asc(),
                studentJpaEntity.studentNumber.studentNumber.asc(),
            ).fetch()

    override fun searchRegisteredStudentsWithPaging(
        id: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: StudentRole?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean?,
        includeGraduates: Boolean,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Page<StudentJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val content =
            jpaQueryFactory
                .selectFrom(studentJpaEntity)
                .innerJoin(accountJpaEntity)
                .on(accountJpaEntity.student.id.eq(studentJpaEntity.id))
                .where(
                    id?.let { studentJpaEntity.id.eq(it) },
                    name?.let { studentJpaEntity.name.contains(it) },
                    email?.let { studentJpaEntity.email.contains(it) },
                    grade?.let { studentJpaEntity.studentNumber.studentGrade.eq(it) },
                    classNum?.let { studentJpaEntity.studentNumber.studentClass.eq(it) },
                    number?.let { studentJpaEntity.studentNumber.studentNumber.eq(it) },
                    sex?.let { studentJpaEntity.sex.eq(it) },
                    role?.let { studentJpaEntity.role.eq(it) },
                    dormitoryRoom?.let { studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { studentJpaEntity.isLeaveSchool.eq(it) },
                    if (!includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(studentJpaEntity.count())
                .from(studentJpaEntity)
                .innerJoin(accountJpaEntity)
                .on(accountJpaEntity.student.id.eq(studentJpaEntity.id))
                .where(
                    id?.let { studentJpaEntity.id.eq(it) },
                    name?.let { studentJpaEntity.name.contains(it) },
                    email?.let { studentJpaEntity.email.contains(it) },
                    grade?.let { studentJpaEntity.studentNumber.studentGrade.eq(it) },
                    classNum?.let { studentJpaEntity.studentNumber.studentClass.eq(it) },
                    number?.let { studentJpaEntity.studentNumber.studentNumber.eq(it) },
                    sex?.let { studentJpaEntity.sex.eq(it) },
                    role?.let { studentJpaEntity.role.eq(it) },
                    dormitoryRoom?.let { studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { studentJpaEntity.isLeaveSchool.eq(it) },
                    if (!includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    override fun findRegisteredStudentsByMajorClub(club: ClubJpaEntity): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(studentJpaEntity.id))
            .where(studentJpaEntity.majorClub.eq(club))
            .fetch()

    override fun findRegisteredStudentsByJobClub(club: ClubJpaEntity): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(studentJpaEntity.id))
            .where(studentJpaEntity.jobClub.eq(club))
            .fetch()

    override fun findRegisteredStudentsByAutonomousClub(club: ClubJpaEntity): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(studentJpaEntity.id))
            .where(studentJpaEntity.autonomousClub.eq(club))
            .fetch()
}
