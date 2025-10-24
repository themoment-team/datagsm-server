package team.themoment.datagsm.domain.student.repository.custom.impl

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.domain.auth.entity.constant.Role
import team.themoment.datagsm.domain.student.entity.QStudentJpaEntity.Companion.studentJpaEntity
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.repository.custom.StudentJpaCustomRepository
import com.querydsl.core.types.dsl.Expressions

@Repository
class StudentJpaCustomRepositoryImpl(
    val jpaQueryFactory: JPAQueryFactory,
) : StudentJpaCustomRepository {
    override fun searchStudentsWithPaging(
        studentId: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: Role?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean,
        pageable: Pageable,
    ): Page<StudentJpaEntity> {

        var searchResult = searchStudentsWithStartsWith(
            studentId, name, email, grade, classNum, number, sex, role, dormitoryRoom, isLeaveSchool, pageable
        )
        if (searchResult.content.isEmpty()) {
            searchResult = searchStudentsWithContains(
                studentId, name, email, grade, classNum, number, sex, role, dormitoryRoom, isLeaveSchool, pageable
            )
        }
        return searchResult
    }

    private fun searchStudentsWithStartsWith(
        studentId: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: Role?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean,
        pageable: Pageable,
    ): Page<StudentJpaEntity> {
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    studentJpaEntity,
                    countExpression.`as`("count"),
                )
                .from(studentJpaEntity)
                .where(
                    studentId?.let { studentJpaEntity.studentId.eq(it) },
                    name?.let { studentJpaEntity.studentName.startsWith(it) },
                    email?.let { studentJpaEntity.studentEmail.startsWith(it) },
                    grade?.let { studentJpaEntity.studentNumber.studentGrade.eq(it) },
                    classNum?.let { studentJpaEntity.studentNumber.studentClass.eq(it) },
                    number?.let { studentJpaEntity.studentNumber.studentNumber.eq(it) },
                    sex?.let { studentJpaEntity.studentSex.eq(it) },
                    role?.let { studentJpaEntity.studentRole.eq(it) },
                    dormitoryRoom?.let { studentJpaEntity.studentDormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    studentJpaEntity.studentIsLeaveSchool.eq(isLeaveSchool),
                )
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val students = queryResult.map { it.get(studentJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(students, pageable) { count }
    }

    private fun searchStudentsWithContains(
        studentId: Long?,
        name: String?,
        email: String?,
        grade: Int?,
        classNum: Int?,
        number: Int?,
        sex: Sex?,
        role: Role?,
        dormitoryRoom: Int?,
        isLeaveSchool: Boolean,
        pageable: Pageable,
    ): Page<StudentJpaEntity> {
        val countExpression = Expressions.numberTemplate(Long::class.javaObjectType, "COUNT(*) OVER()")
        val queryResult =
            jpaQueryFactory
                .select(
                    studentJpaEntity,
                    countExpression.`as`("count"),
                )
                .from(studentJpaEntity)
                .where(
                    studentId?.let { studentJpaEntity.studentId.eq(it) },
                    name?.let { studentJpaEntity.studentName.contains(it) },
                    email?.let { studentJpaEntity.studentEmail.contains(it) },
                    grade?.let { studentJpaEntity.studentNumber.studentGrade.eq(it) },
                    classNum?.let { studentJpaEntity.studentNumber.studentClass.eq(it) },
                    number?.let { studentJpaEntity.studentNumber.studentNumber.eq(it) },
                    sex?.let { studentJpaEntity.studentSex.eq(it) },
                    role?.let { studentJpaEntity.studentRole.eq(it) },
                    dormitoryRoom?.let { studentJpaEntity.studentDormitoryRoomNumber.dormitoryRoomNumber.eq(it) },
                    studentJpaEntity.studentIsLeaveSchool.eq(isLeaveSchool),
                )
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        if (queryResult.isEmpty()) {
            return PageableExecutionUtils.getPage(emptyList(), pageable) { 0L }
        }
        val students = queryResult.map { it.get(studentJpaEntity) }
        val count = queryResult.first().get(countExpression)!!
        return PageableExecutionUtils.getPage(students, pageable) { count }
    }

    override fun existsByStudentEmail(email: String): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(studentJpaEntity.studentEmail.eq(email))
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

    override fun existsByStudentEmailAndNotStudentId(
        email: String,
        studentId: Long,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(
                studentJpaEntity.studentEmail
                    .eq(email)
                    .and(studentJpaEntity.studentId.ne(studentId)),
            ).fetchFirst() != null

    override fun existsByStudentNumberAndNotStudentId(
        grade: Int,
        classNum: Int,
        number: Int,
        studentId: Long,
    ): Boolean =
        jpaQueryFactory
            .selectOne()
            .from(studentJpaEntity)
            .where(
                studentJpaEntity.studentNumber.studentGrade
                    .eq(grade)
                    .and(studentJpaEntity.studentNumber.studentClass.eq(classNum))
                    .and(studentJpaEntity.studentNumber.studentNumber.eq(number))
                    .and(studentJpaEntity.studentId.ne(studentId)),
            ).fetchFirst() != null
}
