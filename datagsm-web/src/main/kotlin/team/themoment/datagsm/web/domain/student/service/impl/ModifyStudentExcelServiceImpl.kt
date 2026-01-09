package team.themoment.datagsm.web.domain.student.service.impl

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.common.dto.student.internal.ExcelColumnDto
import team.themoment.datagsm.common.dto.student.internal.ExcelRowDto
import team.themoment.datagsm.web.domain.student.service.ModifyStudentExcelService
import team.themoment.sdk.exception.ExpectedException
import team.themoment.sdk.response.CommonApiResponse

@Service
class ModifyStudentExcelServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
) : ModifyStudentExcelService {
    private val dataFormatter = DataFormatter()

    override fun execute(file: MultipartFile): CommonApiResponse<Nothing> {
        val excelData: List<ExcelColumnDto> = queryExcelData(file).flatMap { it.excelRows }
        val studentNumbers =
            excelData
                .sortedBy { it.number }
                .map { it.number }
        val studentEmails =
            excelData
                .map { it.email }

        if (studentNumbers.isEmpty()) {
            throw ExpectedException(
                "엑셀 내 모든 학번이 비어있습니다.",
                HttpStatus.BAD_REQUEST,
            )
        }

        val duplicatesStudentNumber =
            studentNumbers
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .keys
        if (duplicatesStudentNumber.isNotEmpty()) {
            throw ExpectedException(
                "엑셀 파일에 다음 학번이 중복으로 존재합니다: $duplicatesStudentNumber",
                HttpStatus.BAD_REQUEST,
            )
        }

        val duplicatesEmail =
            studentEmails
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .keys
        if (duplicatesEmail.isNotEmpty()) {
            throw ExpectedException(
                "엑셀 파일에 다음 이메일이 중복으로 존재합니다: $duplicatesEmail",
                HttpStatus.BAD_REQUEST,
            )
        }

        val existingStudents =
            studentJpaRepository
                .findAllStudents()
                .associateBy { it.studentNumber.fullStudentNumber }

        val dbStudentNumbers = existingStudents.keys
        val excelStudentNumbers = studentNumbers.toSet()

        val missingInDb = excelStudentNumbers - dbStudentNumbers
        if (missingInDb.isNotEmpty()) {
            throw ExpectedException(
                "DB에 존재하지 않는 학번이 엑셀 파일에 존재합니다: $missingInDb",
                HttpStatus.BAD_REQUEST,
            )
        }
        val extraInDb = dbStudentNumbers - excelStudentNumbers
        if (extraInDb.isNotEmpty()) {
            throw ExpectedException(
                "엑셀에 존재하지 않는 학번이 데이터베이스에 존재합니다: $extraInDb",
                HttpStatus.BAD_REQUEST,
            )
        }

        val majorClubs = excelData.mapNotNull { it.majorClub }.distinct()
        val jobClubs = excelData.mapNotNull { it.jobClub }.distinct()
        val autonomousClubs = excelData.mapNotNull { it.autonomousClub }.distinct()

        val existingMajorClubs =
            if (majorClubs.isEmpty()) {
                emptyMap()
            } else {
                clubJpaRepository
                    .findAllByNameInAndType(majorClubs, ClubType.MAJOR_CLUB)
                    .associateBy { it.name }
            }
        val existingJobClubs =
            if (jobClubs.isEmpty()) {
                emptyMap()
            } else {
                clubJpaRepository
                    .findAllByNameInAndType(jobClubs, ClubType.JOB_CLUB)
                    .associateBy { it.name }
            }
        val existingAutonomousClubs =
            if (autonomousClubs.isEmpty()) {
                emptyMap()
            } else {
                clubJpaRepository
                    .findAllByNameInAndType(autonomousClubs, ClubType.AUTONOMOUS_CLUB)
                    .associateBy { it.name }
            }

        val studentsToSave =
            excelData.map { dto ->
                existingStudents.getValue(dto.number).also { student ->
                    student.name = dto.name
                    student.email = "TEMP_${dto.number}"
                    student.major = dto.major
                    student.majorClub =
                        dto.majorClub?.let { clubName ->
                            existingMajorClubs[clubName]
                                ?: throw ExpectedException("존재하지 않는 전공동아리입니다.", HttpStatus.BAD_REQUEST)
                        }
                    student.jobClub =
                        dto.jobClub?.let { clubName ->
                            existingJobClubs[clubName]
                                ?: throw ExpectedException("존재하지 않는 취업동아리입니다.", HttpStatus.BAD_REQUEST)
                        }
                    student.autonomousClub =
                        dto.autonomousClub?.let { clubName ->
                            existingAutonomousClubs[clubName]
                                ?: throw ExpectedException("존재하지 않는 창체동아리입니다.", HttpStatus.BAD_REQUEST)
                        }
                    student.dormitoryRoomNumber = getDormitoryEmbedded(dto.dormitoryRoomNumber)
                    student.role = dto.role
                    student.isLeaveSchool = dto.isLeaveSchool
                    student.sex = dto.sex
                }
            }
        studentJpaRepository.flush()

        excelData.zip(studentsToSave).forEach { (dto, student) ->
            student.email = dto.email
        }

        return CommonApiResponse.success("엑셀 업로드 성공")
    }

    private fun queryExcelData(file: MultipartFile): List<ExcelRowDto> {
        val workbook =
            file.inputStream.use { inputStream ->
                when (file.originalFilename?.substringAfterLast(".")) {
                    "xlsx" -> XSSFWorkbook(inputStream)
                    "xls" -> HSSFWorkbook(inputStream)
                    else -> throw IllegalArgumentException("지원하지 않는 파일 형식입니다.")
                }
            }

        try {
            if (
                workbook.numberOfSheets != 3 ||
                !workbook.getSheetAt(0).sheetName.equals("1학년") ||
                !workbook.getSheetAt(1).sheetName.equals("2학년") ||
                !workbook.getSheetAt(2).sheetName.equals("3학년")
            ) {
                throw ExpectedException(
                    "시트는 1학년, 2학년, 3학년으로 구성되어 있어야 합니다.",
                    HttpStatus.BAD_REQUEST,
                )
            }
            val data = mutableListOf<ExcelRowDto>()
            for (i: Int in 0..2) {
                val sheet = workbook.getSheetAt(i)
                val excelRowDto =
                    ExcelRowDto(
                        sheet.drop(1).map { row ->
                            ExcelColumnDto(
                                name = getRequiredString(row, 0, "학생 이름"),
                                number = checkStudentNumber(getRequiredInt(row, 1, "학번")),
                                email = getRequiredString(row, 2, "이메일"),
                                major =
                                    Major.fromMajor(getRequiredString(row, 3, "학과"))
                                        ?: throw ExpectedException(
                                            "${row.rowNum + 1}행: 학과는 'SW개발과', '스마트IoT과', '인공지능과'여야 합니다.",
                                            HttpStatus.BAD_REQUEST,
                                        ),
                                majorClub = getOptionalString(row, 4),
                                jobClub = getOptionalString(row, 5),
                                autonomousClub = getOptionalString(row, 6),
                                dormitoryRoomNumber = getOptionalInt(row, 7),
                                role =
                                    StudentRole.fromRole(getRequiredString(row, 8, "소속"))
                                        ?: throw ExpectedException(
                                            "${row.rowNum + 1}행: 소속은 '일반학생', '기숙사자치위원회', '학생회'여야 합니다.",
                                            HttpStatus.BAD_REQUEST,
                                        ),
                                isLeaveSchool =
                                    when (getRequiredString(row, 9, "자퇴 여부").uppercase()) {
                                        "O" -> true
                                        "X" -> false
                                        else -> throw ExpectedException(
                                            "${row.rowNum + 1}행: 자퇴 여부는 O 또는 X여야 합니다.",
                                            HttpStatus.BAD_REQUEST,
                                        )
                                    },
                                sex =
                                    Sex.fromSex(getRequiredString(row, 10, "성별"))
                                        ?: throw ExpectedException(
                                            "${row.rowNum + 1}행: 성별은 '남자' 또는 '여자'여야 합니다.",
                                            HttpStatus.BAD_REQUEST,
                                        ),
                            )
                        },
                    )
                data.add(excelRowDto)
            }
            return data.toList()
        } finally {
            workbook.close()
        }
    }

    private fun getCellValue(
        row: Row,
        columnIndex: Int,
    ): String {
        val cell = row.getCell(columnIndex) ?: return ""
        return dataFormatter.formatCellValue(cell).trim()
    }

    private fun getRequiredString(
        row: Row,
        columnIndex: Int,
        fieldName: String,
    ): String =
        getCellValue(row, columnIndex)
            .takeIf { it.isNotBlank() }
            ?: throw ExpectedException(
                "${row.rowNum + 1}행 ${fieldName}이(가) 비어있습니다.",
                HttpStatus.BAD_REQUEST,
            )

    private fun getOptionalString(
        row: Row,
        columnIndex: Int,
    ): String? =
        getCellValue(row, columnIndex)
            .takeIf { it.isNotBlank() }

    private fun getRequiredInt(
        row: Row,
        columnIndex: Int,
        fieldName: String,
    ): Int =
        getCellValue(row, columnIndex).toIntOrNull()
            ?: throw ExpectedException(
                "${row.rowNum + 1}행 ${fieldName}이(가) 비어있습니다.",
                HttpStatus.BAD_REQUEST,
            )

    private fun getOptionalInt(
        row: Row,
        columnIndex: Int,
    ): Int? =
        getCellValue(row, columnIndex)
            .toIntOrNull()

    private fun checkStudentNumber(studentNumber: Int?): Int {
        studentNumber ?: throw ExpectedException("학번이 비어있습니다.", HttpStatus.BAD_REQUEST)
        if (studentNumber !in 1101..3418) throw ExpectedException("학번이 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
        if (studentNumber / 100 % 10 !in 1..4) throw ExpectedException("반은 1~4반이여야 합니다.", HttpStatus.BAD_REQUEST)
        return studentNumber
    }

    private fun getStudentNumberEmbedded(number: Int): StudentNumber =
        StudentNumber(
            number / 1000,
            number / 100 % 10,
            number % 100,
        )

    private fun getDormitoryEmbedded(room: Int?): DormitoryRoomNumber = DormitoryRoomNumber(room)
}
