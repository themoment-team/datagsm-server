package team.themoment.datagsm.domain.student.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.student.entity.QStudentJpaEntity.Companion.studentJpaEntity
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository

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
        isLeaveSchool: Boolean,
        pageable: Pageable,
    ): Page<StudentJpaEntity> {
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
                    studentJpaEntity.isLeaveSchool.eq(isLeaveSchool),
                ).offset(pageable.offset)
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
                    studentJpaEntity.isLeaveSchool.eq(isLeaveSchool),
                )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
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
}
