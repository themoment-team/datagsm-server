package team.themoment.datagsm.common.domain.student.repository.custom.impl

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.QAccountJpaEntity.Companion.accountJpaEntity
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.student.entity.BaseStudent
import team.themoment.datagsm.common.domain.student.entity.EnrolledStudent
import team.themoment.datagsm.common.domain.student.entity.QBaseStudent.Companion.baseStudent
import team.themoment.datagsm.common.domain.student.entity.QEnrolledStudent.Companion.enrolledStudent
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
    ): Page<BaseStudent> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val content =
            jpaQueryFactory
                .selectFrom(baseStudent)
                .where(
                    id?.let { baseStudent.id.eq(it) },
                    name?.let { baseStudent.name.contains(it) },
                    email?.let { baseStudent.email.contains(it) },
                    grade?.let { enrolledStudent.studentNumber.studentGrade.eq(it) },
                    classNum?.let { enrolledStudent.studentNumber.studentClass.eq(it) },
                    number?.let { enrolledStudent.studentNumber.studentNumber.eq(it) },
                    sex?.let { baseStudent.sex.eq(it) },
                    role?.let { baseStudent.role.eq(it) },
                    dormitoryRoom?.let { enrolledStudent.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { baseStudent.isLeaveSchool.eq(it) },
                    if (!includeGraduates) baseStudent.instanceOf(EnrolledStudent::class.java) else null,
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(baseStudent.count())
                .from(baseStudent)
                .where(
                    id?.let { baseStudent.id.eq(it) },
                    name?.let { baseStudent.name.contains(it) },
                    email?.let { baseStudent.email.contains(it) },
                    grade?.let { enrolledStudent.studentNumber.studentGrade.eq(it) },
                    classNum?.let { enrolledStudent.studentNumber.studentClass.eq(it) },
                    number?.let { enrolledStudent.studentNumber.studentNumber.eq(it) },
                    sex?.let { baseStudent.sex.eq(it) },
                    role?.let { baseStudent.role.eq(it) },
                    dormitoryRoom?.let { enrolledStudent.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { baseStudent.isLeaveSchool.eq(it) },
                    if (!includeGraduates) baseStudent.instanceOf(EnrolledStudent::class.java) else null,
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
                            enrolledStudent.studentNumber.studentGrade.asc(),
                            enrolledStudent.studentNumber.studentClass.asc(),
                            enrolledStudent.studentNumber.studentNumber.asc(),
                        )
                    }

                    SortDirection.DESC -> {
                        arrayOf(
                            enrolledStudent.studentNumber.studentGrade.desc(),
                            enrolledStudent.studentNumber.studentClass.desc(),
                            enrolledStudent.studentNumber.studentNumber.desc(),
                        )
                    }
                }
            }

            StudentSortBy.DORMITORY_ROOM -> {
                val path = enrolledStudent.dormitoryRoomNumber.dormitoryRoomNumber
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
                        StudentSortBy.ID -> baseStudent.id
                        StudentSortBy.NAME -> baseStudent.name
                        StudentSortBy.EMAIL -> baseStudent.email
                        StudentSortBy.GRADE -> enrolledStudent.studentNumber.studentGrade
                        StudentSortBy.CLASS_NUM -> enrolledStudent.studentNumber.studentClass
                        StudentSortBy.NUMBER -> enrolledStudent.studentNumber.studentNumber
                        StudentSortBy.MAJOR -> enrolledStudent.major
                        StudentSortBy.ROLE -> baseStudent.role
                        StudentSortBy.SEX -> baseStudent.sex
                        StudentSortBy.IS_LEAVE_SCHOOL -> baseStudent.isLeaveSchool
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
            .from(baseStudent)
            .where(baseStudent.email.eq(email))
            .fetchFirst() != null

    override fun existsByStudentNumber(
        grade: Int,
        classNum: Int,
        number: Int,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(enrolledStudent)
            .where(
                enrolledStudent.studentNumber.studentGrade
                    .eq(grade)
                    .and(enrolledStudent.studentNumber.studentClass.eq(classNum))
                    .and(enrolledStudent.studentNumber.studentNumber.eq(number)),
            ).fetchFirst() != null

    override fun existsByStudentEmailAndNotId(
        email: String,
        id: Long,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(baseStudent)
            .where(
                baseStudent.email
                    .eq(email)
                    .and(baseStudent.id.ne(id)),
            ).fetchFirst() != null

    override fun existsByStudentNumberAndNotId(
        grade: Int,
        classNum: Int,
        number: Int,
        id: Long,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(enrolledStudent)
            .where(
                enrolledStudent.studentNumber.studentGrade
                    .eq(grade)
                    .and(enrolledStudent.studentNumber.studentClass.eq(classNum))
                    .and(enrolledStudent.studentNumber.studentNumber.eq(number))
                    .and(enrolledStudent.id.ne(id)),
            ).fetchFirst() != null

    override fun findStudentsByGrade(grade: Int): List<EnrolledStudent> =
        jpaQueryFactory
            .selectFrom(enrolledStudent)
            .leftJoin(enrolledStudent.majorClub)
            .fetchJoin()
            .leftJoin(enrolledStudent.jobClub)
            .fetchJoin()
            .leftJoin(enrolledStudent.autonomousClub)
            .fetchJoin()
            .where(enrolledStudent.studentNumber.studentGrade.eq(grade))
            .orderBy(
                enrolledStudent.studentNumber.studentClass.asc(),
                enrolledStudent.studentNumber.studentNumber.asc(),
            ).fetch()

    override fun findAllStudents(): List<BaseStudent> =
        jpaQueryFactory
            .selectFrom(baseStudent)
            .fetch()

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
    ): Page<BaseStudent> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        val content =
            jpaQueryFactory
                .selectFrom(baseStudent)
                .innerJoin(accountJpaEntity)
                .on(accountJpaEntity.student.id.eq(baseStudent.id))
                .where(
                    id?.let { baseStudent.id.eq(it) },
                    name?.let { baseStudent.name.contains(it) },
                    email?.let { baseStudent.email.contains(it) },
                    grade?.let { enrolledStudent.studentNumber.studentGrade.eq(it) },
                    classNum?.let { enrolledStudent.studentNumber.studentClass.eq(it) },
                    number?.let { enrolledStudent.studentNumber.studentNumber.eq(it) },
                    sex?.let { baseStudent.sex.eq(it) },
                    role?.let { baseStudent.role.eq(it) },
                    dormitoryRoom?.let { enrolledStudent.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { baseStudent.isLeaveSchool.eq(it) },
                    if (!includeGraduates) baseStudent.instanceOf(EnrolledStudent::class.java) else null,
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        val countQuery =
            jpaQueryFactory
                .select(baseStudent.count())
                .from(baseStudent)
                .innerJoin(accountJpaEntity)
                .on(accountJpaEntity.student.id.eq(baseStudent.id))
                .where(
                    id?.let { baseStudent.id.eq(it) },
                    name?.let { baseStudent.name.contains(it) },
                    email?.let { baseStudent.email.contains(it) },
                    grade?.let { enrolledStudent.studentNumber.studentGrade.eq(it) },
                    classNum?.let { enrolledStudent.studentNumber.studentClass.eq(it) },
                    number?.let { enrolledStudent.studentNumber.studentNumber.eq(it) },
                    sex?.let { baseStudent.sex.eq(it) },
                    role?.let { baseStudent.role.eq(it) },
                    dormitoryRoom?.let { enrolledStudent.dormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    isLeaveSchool?.let { baseStudent.isLeaveSchool.eq(it) },
                    if (!includeGraduates) baseStudent.instanceOf(EnrolledStudent::class.java) else null,
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    override fun findRegisteredStudentsByMajorClub(club: ClubJpaEntity): List<EnrolledStudent> =
        jpaQueryFactory
            .selectFrom(enrolledStudent)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(enrolledStudent.id))
            .where(enrolledStudent.majorClub.eq(club))
            .fetch()

    override fun findRegisteredStudentsByJobClub(club: ClubJpaEntity): List<EnrolledStudent> =
        jpaQueryFactory
            .selectFrom(enrolledStudent)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(enrolledStudent.id))
            .where(enrolledStudent.jobClub.eq(club))
            .fetch()

    override fun findRegisteredStudentsByAutonomousClub(club: ClubJpaEntity): List<EnrolledStudent> =
        jpaQueryFactory
            .selectFrom(enrolledStudent)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(enrolledStudent.id))
            .where(enrolledStudent.autonomousClub.eq(club))
            .fetch()
}
