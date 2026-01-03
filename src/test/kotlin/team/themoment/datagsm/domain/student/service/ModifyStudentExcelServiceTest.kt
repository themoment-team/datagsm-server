package team.themoment.datagsm.domain.student.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.entity.constant.StudentRole
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.impl.ModifyStudentExcelServiceImpl
import team.themoment.datagsm.global.exception.error.ExpectedException
import java.io.ByteArrayOutputStream

class ModifyStudentExcelServiceTest :
    DescribeSpec({

        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var modifyStudentExcelService: ModifyStudentExcelService

        beforeEach {
            mockStudentRepository = mockk<StudentJpaRepository>()
            mockClubRepository = mockk<ClubJpaRepository>()
            modifyStudentExcelService = ModifyStudentExcelServiceImpl(mockStudentRepository, mockClubRepository)
        }

        fun createValidExcelFile(): ByteArray {
            val workbook = XSSFWorkbook()

            // 1학년 시트
            val sheet1 = workbook.createSheet("1학년")
            val header1 = sheet1.createRow(0)
            header1.createCell(0).setCellValue("학생명")
            header1.createCell(1).setCellValue("학번")
            header1.createCell(2).setCellValue("이메일")
            header1.createCell(3).setCellValue("학과")
            header1.createCell(4).setCellValue("전공동아리")
            header1.createCell(5).setCellValue("취업동아리")
            header1.createCell(6).setCellValue("창체동아리")
            header1.createCell(7).setCellValue("호실")
            header1.createCell(8).setCellValue("소속")
            header1.createCell(9).setCellValue("자퇴 여부")
            header1.createCell(10).setCellValue("성별")

            val data1 = sheet1.createRow(1)
            data1.createCell(0).setCellValue("홍길동")
            data1.createCell(1).setCellValue("1101")
            data1.createCell(2).setCellValue("hong@gsm.hs.kr")
            data1.createCell(3).setCellValue("SW개발과")
            data1.createCell(4).setCellValue("SW개발동아리")
            data1.createCell(5).setCellValue("취업동아리A")
            data1.createCell(6).setCellValue("창체동아리B")
            data1.createCell(7).setCellValue("301")
            data1.createCell(8).setCellValue("일반학생")
            data1.createCell(9).setCellValue("X")
            data1.createCell(10).setCellValue("남자")

            // 2학년 시트 (빈 시트)
            val sheet2 = workbook.createSheet("2학년")
            val header2 = sheet2.createRow(0)
            for (i in 0..10) {
                header2.createCell(i).setCellValue(header1.getCell(i).stringCellValue)
            }

            // 3학년 시트 (빈 시트)
            val sheet3 = workbook.createSheet("3학년")
            val header3 = sheet3.createRow(0)
            for (i in 0..10) {
                header3.createCell(i).setCellValue(header1.getCell(i).stringCellValue)
            }

            val output = ByteArrayOutputStream()
            workbook.write(output)
            workbook.close()
            return output.toByteArray()
        }

        describe("ModifyStudentExcelService 클래스의") {
            describe("execute 메서드는") {

                context("정상적인 Excel 파일로 학생 정보를 수정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val existingStudent =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "기존이름"
                            studentNumber = StudentNumber(1, 1, 1)
                            email = "old@gsm.hs.kr"
                            major = Major.AI
                            role = StudentRole.GENERAL_STUDENT
                            isLeaveSchool = false
                            sex = Sex.WOMAN
                        }

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

                    beforeEach {
                        every { mockStudentRepository.findAllStudents() } returns listOf(existingStudent)
                        every { mockClubRepository.findAllByNameInAndType(listOf("SW개발동아리"), ClubType.MAJOR_CLUB) } returns
                            listOf(majorClub)
                        every { mockClubRepository.findAllByNameInAndType(listOf("취업동아리A"), ClubType.JOB_CLUB) } returns
                            listOf(jobClub)
                        every { mockClubRepository.findAllByNameInAndType(listOf("창체동아리B"), ClubType.AUTONOMOUS_CLUB) } returns
                            listOf(autonomousClub)
                        every { mockStudentRepository.flush() } returns Unit
                    }

                    it("학생 정보를 수정하고 성공 메시지를 반환해야 한다") {
                        val result = modifyStudentExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"
                        result.status shouldBe HttpStatus.OK.value()

                        existingStudent.name shouldBe "홍길동"
                        existingStudent.email shouldBe "hong@gsm.hs.kr"
                        existingStudent.major shouldBe Major.SW_DEVELOPMENT
                        existingStudent.majorClub shouldBe majorClub
                        existingStudent.sex shouldBe Sex.MAN
                    }
                }

                context("학번이 중복될 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("학생1")
                    row1.createCell(1).setCellValue("1101")
                    row1.createCell(2).setCellValue("s1@gsm.hs.kr")
                    row1.createCell(3).setCellValue("SW개발과")
                    row1.createCell(8).setCellValue("일반학생")
                    row1.createCell(9).setCellValue("X")
                    row1.createCell(10).setCellValue("남자")

                    val row2 = sheet1.createRow(2)
                    row2.createCell(0).setCellValue("학생2")
                    row2.createCell(1).setCellValue("1101")
                    row2.createCell(2).setCellValue("s2@gsm.hs.kr")
                    row2.createCell(3).setCellValue("SW개발과")
                    row2.createCell(8).setCellValue("일반학생")
                    row2.createCell(9).setCellValue("X")
                    row2.createCell(10).setCellValue("남자")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "엑셀 파일에 다음 학번이 중복으로 존재합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("이메일이 중복될 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("학생1")
                    row1.createCell(1).setCellValue("1101")
                    row1.createCell(2).setCellValue("duplicate@gsm.hs.kr")
                    row1.createCell(3).setCellValue("SW개발과")
                    row1.createCell(8).setCellValue("일반학생")
                    row1.createCell(9).setCellValue("X")
                    row1.createCell(10).setCellValue("남자")

                    val row2 = sheet1.createRow(2)
                    row2.createCell(0).setCellValue("학생2")
                    row2.createCell(1).setCellValue("1102")
                    row2.createCell(2).setCellValue("duplicate@gsm.hs.kr")
                    row2.createCell(3).setCellValue("SW개발과")
                    row2.createCell(8).setCellValue("일반학생")
                    row2.createCell(9).setCellValue("X")
                    row2.createCell(10).setCellValue("남자")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "엑셀 파일에 다음 이메일이 중복으로 존재합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("모든 학번이 비어있을 때") {
                    val workbook = XSSFWorkbook()
                    workbook.createSheet("1학년").createRow(0)
                    workbook.createSheet("2학년").createRow(0)
                    workbook.createSheet("3학년").createRow(0)

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldBe "엑셀 내 모든 학번이 비어있습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("지원하지 않는 파일 형식일 때") {
                    val file =
                        MockMultipartFile(
                            "file",
                            "students.pdf",
                            "application/pdf",
                            "dummy content".toByteArray(),
                        )

                    it("IllegalArgumentException이 발생해야 한다") {
                        val exception =
                            shouldThrow<IllegalArgumentException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldBe "지원하지 않는 파일 형식입니다."
                    }
                }

                context("시트 개수가 3개가 아닐 때") {
                    val workbook = XSSFWorkbook()
                    workbook.createSheet("1학년")
                    workbook.createSheet("2학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldBe "시트는 1학년, 2학년, 3학년으로 구성되어 있어야 합니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("시트 이름이 올바르지 않을 때") {
                    val workbook = XSSFWorkbook()
                    workbook.createSheet("첫학년")
                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldBe "시트는 1학년, 2학년, 3학년으로 구성되어 있어야 합니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("DB에 없는 학번이 Excel에 존재할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    beforeEach {
                        every { mockStudentRepository.findAllStudents() } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "DB에 존재하지 않는 학번이 엑셀 파일에 존재합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("Excel에 없는 학번이 DB에 존재할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val student1 =
                        StudentJpaEntity().apply {
                            studentNumber = StudentNumber(1, 1, 1)
                        }

                    val student2 =
                        StudentJpaEntity().apply {
                            studentNumber = StudentNumber(2, 2, 2)
                        }

                    beforeEach {
                        every { mockStudentRepository.findAllStudents() } returns listOf(student1, student2)
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "엑셀에 존재하지 않는 학번이 데이터베이스에 존재합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("존재하지 않는 전공동아리를 참조할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val existingStudent =
                        StudentJpaEntity().apply {
                            studentNumber = StudentNumber(1, 1, 1)
                        }

                    beforeEach {
                        every { mockStudentRepository.findAllStudents() } returns listOf(existingStudent)
                        every { mockClubRepository.findAllByNameInAndType(any(), ClubType.MAJOR_CLUB) } returns emptyList()
                        every { mockClubRepository.findAllByNameInAndType(any(), ClubType.JOB_CLUB) } returns emptyList()
                        every { mockClubRepository.findAllByNameInAndType(any(), ClubType.AUTONOMOUS_CLUB) } returns
                            emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldBe "존재하지 않는 전공동아리입니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("잘못된 학과 값일 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("홍길동")
                    row1.createCell(1).setCellValue("1101")
                    row1.createCell(2).setCellValue("hong@gsm.hs.kr")
                    row1.createCell(3).setCellValue("잘못된학과")
                    row1.createCell(8).setCellValue("일반학생")
                    row1.createCell(9).setCellValue("X")
                    row1.createCell(10).setCellValue("남자")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "학과는 'SW개발과', '스마트IoT과', '인공지능과'여야 합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("잘못된 소속 값일 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("홍길동")
                    row1.createCell(1).setCellValue("1101")
                    row1.createCell(2).setCellValue("hong@gsm.hs.kr")
                    row1.createCell(3).setCellValue("SW개발과")
                    row1.createCell(8).setCellValue("잘못된소속")
                    row1.createCell(9).setCellValue("X")
                    row1.createCell(10).setCellValue("남자")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "소속은 '일반학생', '기숙사자치위원회', '학생회'여야 합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("잘못된 자퇴 여부 값일 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("홍길동")
                    row1.createCell(1).setCellValue("1101")
                    row1.createCell(2).setCellValue("hong@gsm.hs.kr")
                    row1.createCell(3).setCellValue("SW개발과")
                    row1.createCell(8).setCellValue("일반학생")
                    row1.createCell(9).setCellValue("YES")
                    row1.createCell(10).setCellValue("남자")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "자퇴 여부는 O 또는 X여야 합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("잘못된 성별 값일 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("홍길동")
                    row1.createCell(1).setCellValue("1101")
                    row1.createCell(2).setCellValue("hong@gsm.hs.kr")
                    row1.createCell(3).setCellValue("SW개발과")
                    row1.createCell(8).setCellValue("일반학생")
                    row1.createCell(9).setCellValue("X")
                    row1.createCell(10).setCellValue("기타")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldContain "성별은 '남자' 또는 '여자'여야 합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("학번 범위가 올바르지 않을 때") {
                    val workbook = XSSFWorkbook()
                    val sheet1 = workbook.createSheet("1학년")
                    val header = sheet1.createRow(0)
                    val headerLabels =
                        listOf(
                            "학생명",
                            "학번",
                            "이메일",
                            "학과",
                            "전공동아리",
                            "취업동아리",
                            "창체동아리",
                            "호실",
                            "소속",
                            "자퇴 여부",
                            "성별",
                        )
                    headerLabels.forEachIndexed { idx, label -> header.createCell(idx).setCellValue(label) }

                    val row1 = sheet1.createRow(1)
                    row1.createCell(0).setCellValue("홍길동")
                    row1.createCell(1).setCellValue("9999")
                    row1.createCell(2).setCellValue("hong@gsm.hs.kr")
                    row1.createCell(3).setCellValue("SW개발과")
                    row1.createCell(8).setCellValue("일반학생")
                    row1.createCell(9).setCellValue("X")
                    row1.createCell(10).setCellValue("남자")

                    workbook.createSheet("2학년")
                    workbook.createSheet("3학년")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "students.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyStudentExcelService.execute(file)
                            }

                        exception.message shouldBe "학번이 올바르지 않습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }
            }
        }
    })
