package team.themoment.datagsm.common.domain.student.repository.custom.impl

import com.querydsl.core.types.Expression
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberPath
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import team.themoment.datagsm.common.domain.account.entity.QAccountJpaEntity.Companion.accountJpaEntity
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.student.dto.internal.StudentBulkUpdateDto
import team.themoment.datagsm.common.domain.student.entity.QStudentJpaEntity.Companion.studentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.constant.Major
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
        specialty: String?,
        major: Major?,
        githubId: String?,
        includeGraduates: Boolean,
        includeWithdrawn: Boolean,
        onlyEnrolled: Boolean,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Page<StudentJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        // 1쿼리: 페이지네이션 적용하여 student ID만 조회
        val studentIds =
            jpaQueryFactory
                .select(studentJpaEntity.id)
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
                    specialty?.let { studentJpaEntity.specialty.contains(it) },
                    major?.let { studentJpaEntity.major.eq(it) },
                    githubId?.let { studentJpaEntity.githubId.contains(it) },
                    if (!onlyEnrolled && !includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                    if (!onlyEnrolled && !includeWithdrawn) studentJpaEntity.role.ne(StudentRole.WITHDRAWN) else null,
                    if (onlyEnrolled) studentJpaEntity.role.notIn(StudentRole.GRADUATE, StudentRole.WITHDRAWN) else null,
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        // 2쿼리: ID IN절로 majorClub + autonomousClub 한 번에 fetchJoin
        val content =
            if (studentIds.isEmpty()) {
                emptyList()
            } else {
                jpaQueryFactory
                    .selectFrom(studentJpaEntity)
                    .leftJoin(studentJpaEntity.majorClub)
                    .fetchJoin()
                    .leftJoin(studentJpaEntity.autonomousClub)
                    .fetchJoin()
                    .where(studentJpaEntity.id.`in`(studentIds))
                    .apply { orderSpecifier?.let { orderBy(*it) } }
                    .fetch()
            }

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
                    specialty?.let { studentJpaEntity.specialty.contains(it) },
                    major?.let { studentJpaEntity.major.eq(it) },
                    githubId?.let { studentJpaEntity.githubId.contains(it) },
                    if (!onlyEnrolled && !includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                    if (!onlyEnrolled && !includeWithdrawn) studentJpaEntity.role.ne(StudentRole.WITHDRAWN) else null,
                    if (onlyEnrolled) studentJpaEntity.role.notIn(StudentRole.GRADUATE, StudentRole.WITHDRAWN) else null,
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
            .leftJoin(studentJpaEntity.autonomousClub)
            .fetchJoin()
            .where(
                studentJpaEntity.studentNumber.studentGrade.eq(grade),
                studentJpaEntity.role.ne(StudentRole.GRADUATE),
            ).orderBy(
                studentJpaEntity.studentNumber.studentClass.asc(),
                studentJpaEntity.studentNumber.studentNumber.asc(),
            ).fetch()

    override fun findAllStudentsWithClubs(): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .leftJoin(studentJpaEntity.majorClub)
            .fetchJoin()
            .leftJoin(studentJpaEntity.autonomousClub)
            .fetchJoin()
            .orderBy(
                studentJpaEntity.studentNumber.studentGrade.asc(),
                studentJpaEntity.studentNumber.studentClass.asc(),
                studentJpaEntity.studentNumber.studentNumber.asc(),
            ).fetch()

    override fun findAllGraduates(): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .leftJoin(studentJpaEntity.majorClub)
            .fetchJoin()
            .leftJoin(studentJpaEntity.autonomousClub)
            .fetchJoin()
            .where(studentJpaEntity.role.eq(StudentRole.GRADUATE))
            .orderBy(
                studentJpaEntity.studentNumber.studentGrade.asc(),
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
        specialty: String?,
        major: Major?,
        githubId: String?,
        includeGraduates: Boolean,
        includeWithdrawn: Boolean,
        onlyEnrolled: Boolean,
        pageable: Pageable,
        sortBy: StudentSortBy?,
        sortDirection: SortDirection,
    ): Page<StudentJpaEntity> {
        val orderSpecifier = createOrderSpecifier(sortBy, sortDirection)

        // 1쿼리: 페이지네이션 적용하여 student ID만 조회
        val studentIds =
            jpaQueryFactory
                .select(studentJpaEntity.id)
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
                    specialty?.let { studentJpaEntity.specialty.contains(it) },
                    major?.let { studentJpaEntity.major.eq(it) },
                    githubId?.let { studentJpaEntity.githubId.contains(it) },
                    if (!onlyEnrolled && !includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                    if (!onlyEnrolled && !includeWithdrawn) studentJpaEntity.role.ne(StudentRole.WITHDRAWN) else null,
                    if (onlyEnrolled) studentJpaEntity.role.notIn(StudentRole.GRADUATE, StudentRole.WITHDRAWN) else null,
                ).apply {
                    orderSpecifier?.let { orderBy(*it) }
                }.offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()

        // 2쿼리: ID IN절로 majorClub + autonomousClub 한 번에 fetchJoin
        val content =
            if (studentIds.isEmpty()) {
                emptyList()
            } else {
                jpaQueryFactory
                    .selectFrom(studentJpaEntity)
                    .leftJoin(studentJpaEntity.majorClub)
                    .fetchJoin()
                    .leftJoin(studentJpaEntity.autonomousClub)
                    .fetchJoin()
                    .where(studentJpaEntity.id.`in`(studentIds))
                    .apply { orderSpecifier?.let { orderBy(*it) } }
                    .fetch()
            }

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
                    specialty?.let { studentJpaEntity.specialty.contains(it) },
                    major?.let { studentJpaEntity.major.eq(it) },
                    githubId?.let { studentJpaEntity.githubId.contains(it) },
                    if (!onlyEnrolled && !includeGraduates) studentJpaEntity.role.ne(StudentRole.GRADUATE) else null,
                    if (!onlyEnrolled && !includeWithdrawn) studentJpaEntity.role.ne(StudentRole.WITHDRAWN) else null,
                    if (onlyEnrolled) studentJpaEntity.role.notIn(StudentRole.GRADUATE, StudentRole.WITHDRAWN) else null,
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

    override fun findRegisteredStudentsByAutonomousClub(club: ClubJpaEntity): List<StudentJpaEntity> =
        jpaQueryFactory
            .selectFrom(studentJpaEntity)
            .innerJoin(accountJpaEntity)
            .on(accountJpaEntity.student.id.eq(studentJpaEntity.id))
            .where(studentJpaEntity.autonomousClub.eq(club))
            .fetch()

    override fun bulkUpdateEmails(emailUpdates: Map<Long, String>) {
        if (emailUpdates.isEmpty()) return

        val ids = emailUpdates.keys.toList()
        val tempPairs = ids.map { id -> id to "tmp_$id" }

        jpaQueryFactory
            .update(studentJpaEntity)
            .set(studentJpaEntity.email, buildEmailCaseExpr(tempPairs))
            .where(studentJpaEntity.id.`in`(ids))
            .execute()

        val actualPairs = emailUpdates.entries.map { (id, email) -> id to email }

        jpaQueryFactory
            .update(studentJpaEntity)
            .set(studentJpaEntity.email, buildEmailCaseExpr(actualPairs))
            .where(studentJpaEntity.id.`in`(ids))
            .execute()
    }

    override fun bulkUpdateStudentFields(updates: List<StudentBulkUpdateDto>) {
        if (updates.isEmpty()) return
        val ids = updates.map { it.id }

        // scalar 필드 일괄 UPDATE (CASE WHEN + WHERE id IN)
        jpaQueryFactory
            .update(studentJpaEntity)
            .set(studentJpaEntity.name, buildStringCaseExpr(updates.map { it.id to it.name }, studentJpaEntity.name))
            .set(studentJpaEntity.major, buildComparableCaseExpr(updates.map { it.id to it.major }, studentJpaEntity.major))
            .set(studentJpaEntity.role, buildComparableCaseExpr(updates.map { it.id to it.role }, studentJpaEntity.role))
            .set(studentJpaEntity.sex, buildComparableCaseExpr(updates.map { it.id to it.sex }, studentJpaEntity.sex))
            .set(
                studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber,
                buildNullableIntCaseExpr(
                    updates.map { it.id to it.dormitoryRoomNumber },
                    studentJpaEntity.dormitoryRoomNumber.dormitoryRoomNumber,
                ),
            )
            .where(studentJpaEntity.id.`in`(ids))
            .execute()

        // 동아리 FK 초기화 후 동아리별 그룹으로 재할당
        jpaQueryFactory.update(studentJpaEntity).setNull(studentJpaEntity.majorClub).where(studentJpaEntity.id.`in`(ids)).execute()
        jpaQueryFactory.update(studentJpaEntity).setNull(studentJpaEntity.autonomousClub).where(studentJpaEntity.id.`in`(ids)).execute()

        updates.filter { it.majorClub != null }.groupBy { it.majorClub!! }.forEach { (club, group) ->
            jpaQueryFactory
                .update(studentJpaEntity)
                .set(studentJpaEntity.majorClub, club)
                .where(studentJpaEntity.id.`in`(group.map { it.id }))
                .execute()
        }

        updates.filter { it.autonomousClub != null }.groupBy { it.autonomousClub!! }.forEach { (club, group) ->
            jpaQueryFactory
                .update(studentJpaEntity)
                .set(studentJpaEntity.autonomousClub, club)
                .where(studentJpaEntity.id.`in`(group.map { it.id }))
                .execute()
        }
    }

    override fun bulkClearClubReferences(clubs: List<ClubJpaEntity>) {
        if (clubs.isEmpty()) return

        jpaQueryFactory
            .update(studentJpaEntity)
            .setNull(studentJpaEntity.majorClub)
            .where(studentJpaEntity.majorClub.`in`(clubs))
            .execute()

        jpaQueryFactory
            .update(studentJpaEntity)
            .setNull(studentJpaEntity.autonomousClub)
            .where(studentJpaEntity.autonomousClub.`in`(clubs))
            .execute()
    }

    override fun clearClubReferencesByType(
        club: ClubJpaEntity,
        type: ClubType,
    ) {
        val clubPath =
            when (type) {
                ClubType.MAJOR_CLUB -> studentJpaEntity.majorClub
                ClubType.AUTONOMOUS_CLUB -> studentJpaEntity.autonomousClub
            }
        jpaQueryFactory
            .update(studentJpaEntity)
            .setNull(clubPath)
            .where(clubPath.eq(club))
            .execute()
    }

    override fun bulkAssignClub(
        studentIds: List<Long>,
        club: ClubJpaEntity,
        type: ClubType,
    ) {
        if (studentIds.isEmpty()) return

        val clubPath =
            when (type) {
                ClubType.MAJOR_CLUB -> studentJpaEntity.majorClub
                ClubType.AUTONOMOUS_CLUB -> studentJpaEntity.autonomousClub
            }
        jpaQueryFactory
            .update(studentJpaEntity)
            .set(clubPath, club)
            .where(studentJpaEntity.id.`in`(studentIds))
            .execute()
    }

    private fun buildStringCaseExpr(
        pairs: List<Pair<Long, String>>,
        otherwise: Expression<String>,
    ): Expression<String> =
        pairs
            .drop(1)
            .fold(
                CaseBuilder()
                    .`when`(studentJpaEntity.id.eq(pairs[0].first))
                    .then(pairs[0].second),
            ) { caseWhen, (id, value) ->
                caseWhen.`when`(studentJpaEntity.id.eq(id)).then(value)
            }.otherwise(otherwise)

    private fun <T : Comparable<T>> buildComparableCaseExpr(
        pairs: List<Pair<Long, T?>>,
        otherwise: Expression<T>,
    ): Expression<T> {
        val nonNullPairs = pairs.filter { it.second != null }.map { it.first to it.second!! }
        if (nonNullPairs.isEmpty()) return otherwise

        @Suppress("UNCHECKED_CAST")
        return nonNullPairs
            .drop(1)
            .fold(
                CaseBuilder()
                    .`when`(studentJpaEntity.id.eq(nonNullPairs[0].first))
                    .then(nonNullPairs[0].second),
            ) { caseWhen, (id, value) ->
                caseWhen.`when`(studentJpaEntity.id.eq(id)).then(value)
            }.otherwise(otherwise) as Expression<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildNullableIntCaseExpr(
        pairs: List<Pair<Long, Int?>>,
        path: NumberPath<Int>,
    ): Expression<Int> {
        val first = pairs[0]
        val firstExpr: Expression<Int> =
            first.second?.let { Expressions.constant(it) } ?: Expressions.nullExpression<Int>() as Expression<Int>

        return pairs
            .drop(1)
            .fold(
                CaseBuilder().`when`(studentJpaEntity.id.eq(first.first)).then(firstExpr),
            ) { caseWhen, (id, value) ->
                val expr: Expression<Int> =
                    value?.let { Expressions.constant(it) } ?: Expressions.nullExpression<Int>() as Expression<Int>
                caseWhen.`when`(studentJpaEntity.id.eq(id)).then(expr)
            }.otherwise(path)
    }

    private fun buildEmailCaseExpr(pairs: List<Pair<Long, String>>): Expression<String> =
        pairs
            .drop(1)
            .fold(
                CaseBuilder()
                    .`when`(studentJpaEntity.id.eq(pairs[0].first))
                    .then(pairs[0].second),
            ) { caseWhen, (id, email) ->
                caseWhen.`when`(studentJpaEntity.id.eq(id)).then(email)
            }.otherwise(studentJpaEntity.email)
}
