package team.themoment.datagsm.domain.student.service.impl

import jakarta.transaction.Transactional
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.student.dto.internal.ExcelColumnDto
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.DormitoryRoomNumber
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.QueryStudentExcelService
import team.themoment.datagsm.global.exception.error.ExpectedException

@Service
@Transactional
class QueryStudentExcelServiceImpl(
    private val studentJpaRepository: StudentJpaRepository,
    private val clubJpaRepository: ClubJpaRepository,
) : QueryStudentExcelService {
    override fun queryStudentData(file: MultipartFile) {
        val excelData: List<ExcelColumnDto> = queryExcelData(file).flatMap { it.excelRows }
        val studentNumbers = excelData.map { it.number }.distinct()
        val existingStudents =
            if (studentNumbers.isEmpty()) emptyMap()
            else studentJpaRepository
                .findAllByStudentNumberIn(studentNumbers)
                .associateBy { it.studentNumber.fullStudentNumber }

        val majorClubs = excelData.mapNotNull { it.majorClub }.distinct()
        val jobClubs = excelData.mapNotNull { it.jobClub }.distinct()
        val autonomousClubs = excelData.mapNotNull { it.autonomousClub }.distinct()

        val existingMajorClubs =
            if(majorClubs.isEmpty()) emptyMap()
            else clubJpaRepository
            .findAllByNameInAndType(majorClubs, ClubType.MAJOR_CLUB)
            .associateBy { it.name }
        val existingJobClubs =
            if(jobClubs.isEmpty()) emptyMap()
            else clubJpaRepository
            .findAllByNameInAndType(jobClubs, ClubType.JOB_CLUB)
            .associateBy { it.name }
        val existingAutonomousClubs =
            if(autonomousClubs.isEmpty()) emptyMap()
            else clubJpaRepository
            .findAllByNameInAndType(autonomousClubs, ClubType.AUTONOMOUS_CLUB)
            .associateBy { it.name }

        val studentsToSave = excelData.map { dto ->
            val studentClass: Int = (dto.number % 1000) / 100
            existingStudents[dto.number]?.apply {
                this.name = dto.name
                this.studentNumber = getStudentNumberEmbedded(dto.number)
                this.email = dto.email
                this.major = requireNotNull(Major.fromClassNum(studentClass)) {
                    ExpectedException("학번의 반 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
                }
                // 엑셀에서 존재하지 않는 동아리면 동아리 수정이 안되도록 하였습니다.
                // 이와 관련해서도 추가적인 리뷰 부탁드립니다.
                dto.majorClub?.let { clubName ->
                    existingMajorClubs[clubName]
                        ?: throw ExpectedException("존재하지 않는 전공동아리입니다.", HttpStatus.BAD_REQUEST)
                }
                dto.jobClub?.let { clubName ->
                    existingJobClubs[clubName]
                        ?: throw ExpectedException("존재하지 않는 취업동아리입니다.", HttpStatus.BAD_REQUEST)
                }
                dto.autonomousClub?.let { clubName ->
                    existingAutonomousClubs[clubName]
                        ?: throw ExpectedException("존재하지 않는 창체동아리입니다.", HttpStatus.BAD_REQUEST)
                }
                this.dormitoryRoomNumber = getDormitoryEmbedded(dto.dormitoryRoomNumber)
                this.role = when (dto.role) {
                    "일반인" -> StudentRole.GENERAL_STUDENT
                    "기숙사자치위원회" -> StudentRole.DORMITORY_MANAGER
                    else -> StudentRole.STUDENT_COUNCIL
                }
                this.isLeaveSchool = dto.isLeaveSchool
                this.sex = when (dto.sex) {
                    "남자" -> Sex.MAN
                    else -> Sex.WOMAN
                }
            } ?: StudentJpaEntity().apply {
                this.name = dto.name
                this.studentNumber = getStudentNumberEmbedded(dto.number)
                this.email = dto.email
                this.major = requireNotNull(Major.fromClassNum(studentClass)) {
                    ExpectedException("학번의 반 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
                }
                // 엑셀에서 존재하지 않는 동아리면 동아리 수정이 안되도록 하였습니다.
                // 이와 관련해서도 추가적인 리뷰 부탁드립니다.
                dto.majorClub?.let { clubName ->
                    existingMajorClubs[clubName]
                        ?: throw ExpectedException("존재하지 않는 전공동아리입니다.", HttpStatus.BAD_REQUEST)
                }
                dto.jobClub?.let { clubName ->
                    existingJobClubs[clubName]
                        ?: throw ExpectedException("존재하지 않는 취업동아리입니다.", HttpStatus.BAD_REQUEST)
                }
                dto.autonomousClub?.let { clubName ->
                    existingAutonomousClubs[clubName]
                        ?: throw ExpectedException("존재하지 않는 창체동아리입니다.", HttpStatus.BAD_REQUEST)
                }
                this.dormitoryRoomNumber = getDormitoryEmbedded(dto.dormitoryRoomNumber)
                this.role = when (dto.role) {
                    "일반인" -> StudentRole.GENERAL_STUDENT
                    "기숙사자치위원회" -> StudentRole.DORMITORY_MANAGER
                    else -> StudentRole.STUDENT_COUNCIL
                }
                this.isLeaveSchool = dto.isLeaveSchool
                this.sex = when (dto.sex) {
                    "남자" -> Sex.MAN
                    else -> Sex.WOMAN
                }
            }
        }
        studentJpaRepository.saveAll(studentsToSave)
    }

    override fun queryExcelData(file: MultipartFile): List<ExcelRowDto> {
        val workbook = file.inputStream.use { inputStream ->
            when (file.originalFilename?.substringAfterLast(".")) {
                "xlsx" -> XSSFWorkbook(inputStream)
                "xls" -> HSSFWorkbook(inputStream)
                else -> throw IllegalArgumentException("지원하지 않는 파일 형식입니다.")
            }
        }

        try {
            if(
                workbook.numberOfSheets != 3 ||
                !workbook.getSheetAt(0).sheetName.equals("1학년") ||
                !workbook.getSheetAt(1).sheetName.equals("2학년") ||
                !workbook.getSheetAt(2).sheetName.equals("3학년")
            ) {
                throw ExpectedException(
                    "시트는 1학년, 2학년, 3학년으로 구성되어 있어야 합니다.",
                    HttpStatus.BAD_REQUEST)
            }
            val data = mutableListOf<ExcelRowDto>()
            for (i: Int in 0..2) {
                val sheet = workbook.getSheetAt(i)
                val excelRowDto = ExcelRowDto(
                    sheet.drop(1).map { row ->
                        ExcelColumnDto(
                            name = row.getCell(1)?.stringCellValue?.trim()
                                ?: throw ExpectedException("학생 이름이 비어있습니다.", HttpStatus.BAD_REQUEST),
                            number = checkStudentNumber(row.getCell(0)?.numericCellValue?.toInt()),
                            email = row.getCell(3)?.stringCellValue?.trim()
                                ?: throw ExpectedException("이메일이 비어있습니다.", HttpStatus.BAD_REQUEST),
                            major = row.getCell(4)?.stringCellValue?.trim()
                                ?.takeIf { listOf("SW개발과", "스마트IoT과", "인공지능과").contains(it) }
                                ?: throw ExpectedException("학과 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
                            majorClub = row.getCell(5)?.stringCellValue?.trim(),
                            jobClub = row.getCell(6)?.stringCellValue?.trim(),
                            autonomousClub = row.getCell(7)?.stringCellValue?.trim(),
                            dormitoryRoomNumber = row.getCell(9)?.numericCellValue?.toInt(),
                            role = row.getCell(8)?.stringCellValue?.trim()
                                ?.takeIf { listOf("일반인", "기숙사자치위원회", "학생회").contains(it) }
                                ?: throw ExpectedException("소속이 비어있습니다.", HttpStatus.BAD_REQUEST),
                            isLeaveSchool = when (row.getCell(10)?.stringCellValue?.trim()?.uppercase()) {
                                null -> throw ExpectedException("자퇴 여부는 필수입니다.", HttpStatus.BAD_REQUEST)
                                "O" -> true
                                "X" -> false
                                else -> throw ExpectedException("자퇴 여부는 O 또는 X를 사용해야 합니다.", HttpStatus.BAD_REQUEST)
                            },
                            sex = row.getCell(2)?.stringCellValue?.trim()
                                ?.takeIf { listOf("남자", "여자").contains(it) }
                                ?: throw ExpectedException("성별이 비어있습니다.", HttpStatus.BAD_REQUEST),
                        )
                    }
                )
                data.add(excelRowDto)
            }
            return data.toList()
        } finally {
            workbook.close()
        }
    }

    fun checkStudentNumber(studentNumber: Int?): Int {
        studentNumber ?: throw ExpectedException("학번이 비어있습니다.", HttpStatus.BAD_REQUEST)
        if (studentNumber !in 1101..3418) throw ExpectedException("학번이 올바르지 않습니다.", HttpStatus.BAD_REQUEST)
        if (studentNumber / 100 % 10 !in 1..4) throw ExpectedException("반은 1~4반이여야 합니다.", HttpStatus.BAD_REQUEST)
        return studentNumber
    }

    fun getStudentNumberEmbedded(number: Int): StudentNumber {
        return StudentNumber(
            number / 1000,
            number / 100 % 10,
            number % 100
        )
    }

    fun getDormitoryEmbedded(room: Int?): DormitoryRoomNumber {
        return DormitoryRoomNumber(room)
    }
}
