package team.themoment.datagsm.domain.student.service.impl

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.domain.student.dto.internal.ExcelColumnDto
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.CreateStudentExcelService
import java.io.ByteArrayOutputStream

@Service
@Transactional(readOnly = true)
class CreateStudentExcelServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
) : CreateStudentExcelService {
    override fun createExcel(): ByteArray {
        val data: List<ExcelRowDto> = getStudentData()
        val workbook = XSSFWorkbook()

        data.forEachIndexed { idx, excelRowDto ->
            val sheet = workbook.createSheet("${idx + 1}학년")

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("학생명")
            headerRow.createCell(1).setCellValue("학번")
            headerRow.createCell(2).setCellValue("이메일")
            headerRow.createCell(3).setCellValue("학과")
            headerRow.createCell(4).setCellValue("전공동아리")
            headerRow.createCell(5).setCellValue("취업동아리")
            headerRow.createCell(6).setCellValue("창체동아리")
            headerRow.createCell(7).setCellValue("호실")
            headerRow.createCell(8).setCellValue("소속")
            headerRow.createCell(9).setCellValue("자퇴 여부")
            headerRow.createCell(10).setCellValue("성별")

            excelRowDto.excelRows.forEachIndexed { rowIndex, columnDto ->
                val row = sheet.createRow(rowIndex + 1)

                row.createCell(0).setCellValue(columnDto.name)
                row.createCell(1).setCellValue(columnDto.number.toString())
                row.createCell(2).setCellValue(columnDto.email)
                row.createCell(3).setCellValue(columnDto.major.value)
                row.createCell(4).setCellValue(columnDto.majorClub ?: "")
                row.createCell(5).setCellValue(columnDto.jobClub ?: "")
                row.createCell(6).setCellValue(columnDto.autonomousClub ?: "")
                row.createCell(7).setCellValue(columnDto.dormitoryRoomNumber?.toString() ?: "")
                row.createCell(8).setCellValue(columnDto.role.value)
                row.createCell(9).setCellValue(if(columnDto.isLeaveSchool) "O" else "X")
                row.createCell(10).setCellValue(columnDto.sex.value)
            }
        }

        return ByteArrayOutputStream().use { outputStream ->
            workbook.write(outputStream)
            workbook.close()
            outputStream.toByteArray()
        }
    }

    override fun getStudentData(): List<ExcelRowDto> {
        val data = mutableListOf<ExcelRowDto>()
        for (i: Int in 1..3) {
            val list = studentJpaRepository.findStudentsByGrade(i)
            val excelRowDto = ExcelRowDto(
                excelRows = list.map { student ->
                    ExcelColumnDto(
                        name = student.name,
                        number = student.studentNumber.fullStudentNumber,
                        email = student.email,
                        major = student.major,
                        majorClub = student.majorClub?.name ,
                        jobClub = student.jobClub?.name,
                        autonomousClub = student.autonomousClub?.name,
                        dormitoryRoomNumber = student.dormitoryRoomNumber.dormitoryRoomNumber,
                        role = student.role,
                        isLeaveSchool = student.isLeaveSchool,
                        sex = student.sex,
                    )
                }
            )
            data.add(excelRowDto)
        }
        return data.toList()
    }
}
