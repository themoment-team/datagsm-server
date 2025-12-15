package team.themoment.datagsm.domain.neis.schedule.service.impl

import org.springframework.stereotype.Service
import team.themoment.datagsm.domain.neis.schedule.dto.response.ScheduleResDto
import team.themoment.datagsm.domain.neis.schedule.repository.ScheduleRedisRepository
import team.themoment.datagsm.domain.neis.schedule.service.SearchScheduleService
import java.time.LocalDate

@Service
class SearchScheduleServiceImpl(
    private val scheduleRedisRepository: ScheduleRedisRepository,
) : SearchScheduleService {
    override fun execute(
        date: LocalDate?,
        fromDate: LocalDate?,
        toDate: LocalDate?,
    ): List<ScheduleResDto> {
        val schedules = scheduleRedisRepository.findAll()

        return schedules
            .filter { schedule ->
                when {
                    date != null -> schedule.date == date
                    fromDate != null && toDate != null -> schedule.date in fromDate..toDate
                    fromDate != null -> schedule.date >= fromDate
                    toDate != null -> schedule.date <= toDate
                    else -> true
                }
            }.map { schedule ->
                ScheduleResDto(
                    scheduleId = schedule.Id,
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
            }
    }
}
