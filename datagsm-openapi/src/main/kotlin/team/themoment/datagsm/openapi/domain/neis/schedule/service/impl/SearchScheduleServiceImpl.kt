package team.themoment.datagsm.openapi.domain.neis.schedule.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.common.domain.neis.dto.schedule.request.QueryScheduleReqDto
import team.themoment.datagsm.common.domain.neis.dto.schedule.response.ScheduleInfoResDto
import team.themoment.datagsm.common.domain.neis.dto.schedule.response.ScheduleResDto
import team.themoment.datagsm.common.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.openapi.domain.neis.schedule.service.SearchScheduleService

@Service
class SearchScheduleServiceImpl(
    private val scheduleRedisRepository: ScheduleRedisRepository,
) : SearchScheduleService {
    override fun execute(reqDto: QueryScheduleReqDto): ScheduleResDto {
        val date = reqDto.date
        val fromDate = reqDto.fromDate
        val toDate = reqDto.toDate

        val schedules =
            when {
                date != null -> scheduleRedisRepository.findByDate(date)
                fromDate != null && toDate != null ->
                    scheduleRedisRepository.findByDateBetween(fromDate, toDate)
                fromDate != null ->
                    scheduleRedisRepository.findByDateGreaterThanEqual(fromDate)
                toDate != null ->
                    scheduleRedisRepository.findByDateLessThanEqual(toDate)
                else -> scheduleRedisRepository.findAll().toList()
            }

        // Redis 세트에서 읽어 순서가 매번 달라지므로, 같은 날 여러 일정이 일정한 순서로 나오게 정렬한다.
        // 행사명은 id와 같은 기준(trim)으로 비교하고, 행사명까지 같으면 id로 순서를 고정한다.
        val sortedSchedules = schedules.sortedWith(compareBy({ it.date }, { it.eventName.trim() }, { it.id }))

        return ScheduleResDto(
            schedules =
                sortedSchedules.map { schedule ->
                    ScheduleInfoResDto(
                        scheduleId = schedule.id,
                        schoolCode = schedule.schoolCode,
                        schoolName = schedule.schoolName,
                        officeCode = schedule.officeCode,
                        officeName = schedule.officeName,
                        scheduleDate = schedule.date,
                        academicYear = schedule.academicYear,
                        eventName = schedule.eventName,
                        eventContent = schedule.eventContent,
                        dayCategory = schedule.dayCategory,
                        schoolCourseType = schedule.schoolCourseType,
                        dayNightType = schedule.dayNightType,
                        targetGrades = schedule.targetGrades,
                    )
                },
        )
    }
}
