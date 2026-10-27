package team.themoment.datagsm.common.domain.neis.schedule.repository

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.redis.core.RedisKeyValueTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import team.themoment.datagsm.common.domain.neis.schedule.entity.ScheduleRedisEntity
import java.time.LocalDate
import java.util.Optional

class ScheduleRedisCustomRepositoryImplTest :
    DescribeSpec({

        fun schedule(
            id: String,
            date: LocalDate,
            targetGrades: List<Int>,
        ) = ScheduleRedisEntity(
            id = id,
            schoolCode = "7380292",
            schoolName = "광주소프트웨어마이스터고등학교",
            officeCode = "F10",
            officeName = "광주광역시교육청",
            date = date,
            academicYear = "2026",
            eventName = "중간고사",
            eventContent = null,
            dayCategory = null,
            schoolCourseType = null,
            dayNightType = null,
            targetGrades = targetGrades,
        )

        describe("ScheduleRedisCustomRepositoryImpl 클래스의") {

            val setOperations = mockk<SetOperations<String, String>>()
            val redisTemplate = mockk<RedisTemplate<String, String>> { every { opsForSet() } returns setOperations }
            val redisKeyValueTemplate = mockk<RedisKeyValueTemplate>()
            val repository = ScheduleRedisCustomRepositoryImpl(redisTemplate, redisKeyValueTemplate)

            describe("findByDateBetween 메서드는") {

                context("기간 안의 날짜 인덱스에 학사일정 id가 있을 때") {
                    it("저장할 때와 같은 변환기로 읽어 대상 학년 목록을 그대로 반환해야 한다") {
                        val day1 = LocalDate.of(2026, 10, 27)
                        val day2 = day1.plusDays(1)
                        every { setOperations.members("schedule:date:$day1") } returns setOf("s1")
                        every { setOperations.members("schedule:date:$day2") } returns setOf("s2", "expired")
                        every { redisKeyValueTemplate.findById("s1", ScheduleRedisEntity::class.java) } returns
                            Optional.of(schedule("s1", day1, listOf(1, 2)))
                        every { redisKeyValueTemplate.findById("s2", ScheduleRedisEntity::class.java) } returns
                            Optional.of(schedule("s2", day2, emptyList()))
                        every { redisKeyValueTemplate.findById("expired", ScheduleRedisEntity::class.java) } returns Optional.empty()

                        val result = repository.findByDateBetween(day1, day2)

                        result.map { it.id } shouldContainExactlyInAnyOrder listOf("s1", "s2")
                        result.first { it.id == "s1" }.targetGrades shouldBe listOf(1, 2)
                        result.first { it.id == "s2" }.targetGrades shouldBe emptyList()
                    }
                }
            }
        }
    })
