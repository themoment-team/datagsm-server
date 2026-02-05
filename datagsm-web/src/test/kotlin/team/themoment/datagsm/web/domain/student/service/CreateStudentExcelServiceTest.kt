package team.themoment.datagsm.web.domain.student.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.common.domain.club.entity.constant.ClubType
import team.themoment.datagsm.common.domain.student.entity.DormitoryRoomNumber
import team.themoment.datagsm.common.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.common.domain.student.entity.StudentNumber
import team.themoment.datagsm.common.domain.student.entity.constant.Major
import team.themoment.datagsm.common.domain.student.entity.constant.Sex
import team.themoment.datagsm.common.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.common.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.web.domain.student.service.impl.CreateStudentExcelServiceImpl
import java.io.ByteArrayInputStream

private const val NAME_COL_IDX = 0
private const val STUDENT_NUMBER_COL_IDX = 1
private const val EMAIL_COL_IDX = 2
private const val MAJOR_COL_IDX = 3
private const val MAJOR_CLUB_COL_IDX = 4
private const val JOB_CLUB_COL_IDX = 5
private const val AUTONOMOUS_COL_IDX = 6
private const val DORMITORY_ROOM_NUMBER_COL_IDX = 7
private const val STUDENT_ROLE_COL_IDX = 8
private const val SEX_COL_IDX = 9

class CreateStudentExcelServiceTest :
    DescribeSpec({

        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var createStudentExcelService: CreateStudentExcelService

        beforeEach {
            mockStudentRepository = mockk<StudentJpaRepository>()
            createStudentExcelService = CreateStudentExcelServiceImpl(mockStudentRepository)
        }

        describe("CreateStudentExcelService 클래스의") {
            describe("execute 메서드는") {

                context("정상적인 학생 데이터로 Excel을 생성할 때") {
                    val majorClub =
                        ClubJpaEntity().apply {
                            id = 1L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val jobClub =
                        ClubJpaEntity().apply {
                            id = 2L
                            name = "취업동아리A"
                            type = ClubType.JOB_CLUB
                        }

                    val autonomousClub =
                        ClubJpaEntity().apply {
                            id = 3L
                            name = "창체동아리B"
                            type = ClubType.AUTONOMOUS_CLUB
                        }

                    val grade1Students =
                        listOf(
                            StudentJpaEntity().apply {
                                id = 1L
                                name = "홍길동"
                                studentNumber = StudentNumber(1, 1, 1)
                                email = "hong@gsm.hs.kr"
                                major = Major.SW_DEVELOPMENT
                                this.majorClub = majorClub
                                this.jobClub = jobClub
                                this.autonomousClub = autonomousClub
                                dormitoryRoomNumber = DormitoryRoomNumber(301)
                                role = StudentRole.GENERAL_STUDENT
                                sex = Sex.MAN
                            },
                        )

                    val grade2Students =
                        listOf(
                            StudentJpaEntity().apply {
                                id = 2L
                                name = "김철수"
                                studentNumber = StudentNumber(2, 2, 5)
                                email = "kim@gsm.hs.kr"
                                major = Major.AI
                                this.majorClub = null
                                this.jobClub = null
                                this.autonomousClub = null
                                dormitoryRoomNumber = null
                                role = StudentRole.STUDENT_COUNCIL
                                sex = Sex.MAN
                            },
                        )

                    val grade3Students =
                        listOf(
                            StudentJpaEntity().apply {
                                id = 3L
                                name = "이영희"
                                studentNumber = StudentNumber(3, 3, 10)
                                email = "lee@gsm.hs.kr"
                                major = Major.SMART_IOT
                                this.majorClub = null
                                this.jobClub = null
                                this.autonomousClub = null
                                dormitoryRoomNumber = DormitoryRoomNumber(401)
                                role = StudentRole.DORMITORY_MANAGER
                                sex = Sex.WOMAN
                            },
                        )

                    beforeEach {
                        every { mockStudentRepository.findStudentsByGrade(1) } returns grade1Students
                        every { mockStudentRepository.findStudentsByGrade(2) } returns grade2Students
                        every { mockStudentRepository.findStudentsByGrade(3) } returns grade3Students
                    }

                    it("학년별 시트가 포함된 Excel 파일을 생성해야 한다") {
                        val result = createStudentExcelService.execute()

                        result.statusCode shouldBe HttpStatus.OK
                        result.body shouldNotBe null
                        result.headers.contentType.toString() shouldContain
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        result.headers.contentDisposition.filename shouldMatch Regex("학생_현황_\\d{8}\\.xlsx")

                        verify(exactly = 1) { mockStudentRepository.findStudentsByGrade(1) }
                        verify(exactly = 1) { mockStudentRepository.findStudentsByGrade(2) }
                        verify(exactly = 1) { mockStudentRepository.findStudentsByGrade(3) }

                        // Excel 내용 검증
                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))

                        // 3개의 시트가 있어야 함
                        workbook.numberOfSheets shouldBe 3
                        workbook.getSheetAt(0).sheetName shouldBe "1학년"
                        workbook.getSheetAt(1).sheetName shouldBe "2학년"
                        workbook.getSheetAt(2).sheetName shouldBe "3학년"

                        // 1학년 시트 검증
                        val sheet1 = workbook.getSheetAt(0)
                        val header1 = sheet1.getRow(0)
                        header1.getCell(NAME_COL_IDX).stringCellValue shouldBe "학생명"
                        header1.getCell(STUDENT_NUMBER_COL_IDX).stringCellValue shouldBe "학번"
                        header1.getCell(EMAIL_COL_IDX).stringCellValue shouldBe "이메일"
                        header1.getCell(MAJOR_COL_IDX).stringCellValue shouldBe "학과"
                        header1.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe "전공동아리"
                        header1.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe "취업동아리"
                        header1.getCell(AUTONOMOUS_COL_IDX).stringCellValue shouldBe "창체동아리"
                        header1.getCell(DORMITORY_ROOM_NUMBER_COL_IDX).stringCellValue shouldBe "호실"
                        header1.getCell(STUDENT_ROLE_COL_IDX).stringCellValue shouldBe "소속"
                        header1.getCell(SEX_COL_IDX).stringCellValue shouldBe "성별"

                        val data1 = sheet1.getRow(1)
                        data1.getCell(NAME_COL_IDX).stringCellValue shouldBe "홍길동"
                        data1.getCell(STUDENT_NUMBER_COL_IDX).stringCellValue shouldBe "1101"
                        data1.getCell(EMAIL_COL_IDX).stringCellValue shouldBe "hong@gsm.hs.kr"
                        data1.getCell(MAJOR_COL_IDX).stringCellValue shouldBe "SW개발과"
                        data1.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe "SW개발동아리"
                        data1.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe "취업동아리A"
                        data1.getCell(AUTONOMOUS_COL_IDX).stringCellValue shouldBe "창체동아리B"
                        data1.getCell(DORMITORY_ROOM_NUMBER_COL_IDX).stringCellValue shouldBe "301"
                        data1.getCell(STUDENT_ROLE_COL_IDX).stringCellValue shouldBe "일반학생"
                        data1.getCell(SEX_COL_IDX).stringCellValue shouldBe "남자"

                        // 2학년 시트 검증 (동아리 없음)
                        val sheet2 = workbook.getSheetAt(1)
                        val data2 = sheet2.getRow(1)
                        data2.getCell(NAME_COL_IDX).stringCellValue shouldBe "김철수"
                        data2.getCell(MAJOR_CLUB_COL_IDX).stringCellValue shouldBe ""
                        data2.getCell(JOB_CLUB_COL_IDX).stringCellValue shouldBe ""
                        data2.getCell(AUTONOMOUS_COL_IDX).stringCellValue shouldBe ""
                        data2.getCell(DORMITORY_ROOM_NUMBER_COL_IDX).stringCellValue shouldBe ""
                        data2.getCell(STUDENT_ROLE_COL_IDX).stringCellValue shouldBe "학생회"

                        val sheet3 = workbook.getSheetAt(2)
                        val data3 = sheet3.getRow(1)
                        data3.getCell(NAME_COL_IDX).stringCellValue shouldBe "이영희"
                        data3.getCell(SEX_COL_IDX).stringCellValue shouldBe "여자"

                        workbook.close()
                    }
                }

                context("빈 학생 리스트로 Excel을 생성할 때") {
                    beforeEach {
                        every { mockStudentRepository.findStudentsByGrade(1) } returns emptyList()
                        every { mockStudentRepository.findStudentsByGrade(2) } returns emptyList()
                        every { mockStudentRepository.findStudentsByGrade(3) } returns emptyList()
                    }

                    it("헤더만 있는 3개의 시트를 생성해야 한다") {
                        val result = createStudentExcelService.execute()

                        result.statusCode shouldBe HttpStatus.OK
                        result.body shouldNotBe null

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))

                        workbook.numberOfSheets shouldBe 3

                        // 각 시트는 헤더만 존재
                        for (i in 0..2) {
                            val sheet = workbook.getSheetAt(i)
                            sheet.lastRowNum shouldBe 0
                        }

                        workbook.close()
                    }
                }

                context("다수의 학생이 있을 때") {
                    val students =
                        (1..30).map { idx ->
                            StudentJpaEntity().apply {
                                id = idx.toLong()
                                name = "학생$idx"
                                studentNumber = StudentNumber(2, 1, idx)
                                email = "student$idx@gsm.hs.kr"
                                major = Major.SW_DEVELOPMENT
                                majorClub = null
                                jobClub = null
                                autonomousClub = null
                                dormitoryRoomNumber = DormitoryRoomNumber(200 + idx)
                                role = StudentRole.GENERAL_STUDENT
                                sex = if (idx % 2 == 0) Sex.WOMAN else Sex.MAN
                            }
                        }

                    beforeEach {
                        every { mockStudentRepository.findStudentsByGrade(1) } returns emptyList()
                        every { mockStudentRepository.findStudentsByGrade(2) } returns students
                        every { mockStudentRepository.findStudentsByGrade(3) } returns emptyList()
                    }

                    it("모든 학생이 포함된 Excel 파일을 생성해야 한다") {
                        val result = createStudentExcelService.execute()

                        result.statusCode shouldBe HttpStatus.OK

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))
                        val sheet2 = workbook.getSheetAt(1)

                        // 헤더 + 30명
                        sheet2.lastRowNum shouldBe 30

                        // 첫 번째 학생
                        val firstRow = sheet2.getRow(1)
                        firstRow.getCell(NAME_COL_IDX).stringCellValue shouldBe "학생1"

                        // 마지막 학생
                        val lastRow = sheet2.getRow(30)
                        lastRow.getCell(NAME_COL_IDX).stringCellValue shouldBe "학생30"

                        workbook.close()
                    }
                }

                context("파일명 형식을 검증할 때") {
                    beforeEach {
                        every { mockStudentRepository.findStudentsByGrade(any()) } returns emptyList()
                    }

                    it("파일명은 '학생_현황_yyyyMMdd.xlsx' 형식이어야 한다") {
                        val result = createStudentExcelService.execute()

                        val filename = result.headers.contentDisposition.filename
                        filename shouldNotBe null
                        filename!! shouldMatch Regex("학생_현황_\\d{8}\\.xlsx")
                    }
                }

                context("각 학년별로 다른 수의 학생이 있을 때") {
                    val grade1Students =
                        (1..5).map { idx ->
                            StudentJpaEntity().apply {
                                id = idx.toLong()
                                name = "1학년학생$idx"
                                studentNumber = StudentNumber(1, 1, idx)
                                email = "g1s$idx@gsm.hs.kr"
                                major = Major.SW_DEVELOPMENT
                                role = StudentRole.GENERAL_STUDENT
                                sex = Sex.MAN
                            }
                        }

                    val grade2Students =
                        (1..10).map { idx ->
                            StudentJpaEntity().apply {
                                id = (idx + 5).toLong()
                                name = "2학년학생$idx"
                                studentNumber = StudentNumber(2, 1, idx)
                                email = "g2s$idx@gsm.hs.kr"
                                major = Major.AI
                                role = StudentRole.GENERAL_STUDENT
                                sex = Sex.WOMAN
                            }
                        }

                    val grade3Students =
                        (1..3).map { idx ->
                            StudentJpaEntity().apply {
                                id = (idx + 15).toLong()
                                name = "3학년학생$idx"
                                studentNumber = StudentNumber(3, 1, idx)
                                email = "g3s$idx@gsm.hs.kr"
                                major = Major.SMART_IOT
                                role = StudentRole.GENERAL_STUDENT
                                sex = Sex.MAN
                            }
                        }

                    beforeEach {
                        every { mockStudentRepository.findStudentsByGrade(1) } returns grade1Students
                        every { mockStudentRepository.findStudentsByGrade(2) } returns grade2Students
                        every { mockStudentRepository.findStudentsByGrade(3) } returns grade3Students
                    }

                    it("각 시트에 해당 학년의 학생 수만큼 행을 생성해야 한다") {
                        val result = createStudentExcelService.execute()

                        val workbook = XSSFWorkbook(ByteArrayInputStream(result.body))

                        val sheet1 = workbook.getSheetAt(0)
                        val sheet2 = workbook.getSheetAt(1)
                        val sheet3 = workbook.getSheetAt(2)

                        sheet1.lastRowNum shouldBe 5
                        sheet2.lastRowNum shouldBe 10
                        sheet3.lastRowNum shouldBe 3

                        workbook.close()
                    }
                }
            }
        }
    })
