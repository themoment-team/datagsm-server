package team.themoment.datagsm.domain.club.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.themoment.datagsm.domain.club.entity.ClubJpaEntity
import team.themoment.datagsm.domain.club.entity.constant.ClubType
import team.themoment.datagsm.domain.club.repository.ClubJpaRepository
import team.themoment.datagsm.domain.club.service.impl.ModifyClubExcelServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.domain.student.entity.constant.Major
import team.themoment.datagsm.domain.student.entity.constant.Sex
import team.themoment.datagsm.domain.student.entity.constant.StudentNumber
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.global.exception.error.ExpectedException
import java.io.ByteArrayOutputStream

class ModifyClubExcelServiceTest :
    DescribeSpec({

        lateinit var mockClubRepository: ClubJpaRepository
        lateinit var mockStudentRepository: StudentJpaRepository
        lateinit var modifyClubExcelService: ModifyClubExcelService

        beforeEach {
            mockClubRepository = mockk<ClubJpaRepository>()
            mockStudentRepository = mockk<StudentJpaRepository>()
            modifyClubExcelService = ModifyClubExcelServiceImpl(mockClubRepository, mockStudentRepository)
        }

        fun createValidExcelFile(): ByteArray {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("동아리")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("전공동아리")
            headerRow.createCell(1).setCellValue("전공동아리 부장")
            headerRow.createCell(2).setCellValue("취업동아리")
            headerRow.createCell(3).setCellValue("취업동아리 부장")
            headerRow.createCell(4).setCellValue("창체동아리")
            headerRow.createCell(5).setCellValue("창체동아리 부장")

            val dataRow = sheet.createRow(1)
            dataRow.createCell(0).setCellValue("SW개발동아리")
            dataRow.createCell(1).setCellValue("2404 김철수")
            dataRow.createCell(2).setCellValue("취업동아리A")
            dataRow.createCell(3).setCellValue("2305 이영희")
            dataRow.createCell(4).setCellValue("창체동아리B")
            dataRow.createCell(5).setCellValue("1210 박민수")

            val output = ByteArrayOutputStream()
            workbook.write(output)
            workbook.close()
            return output.toByteArray()
        }

        describe("ModifyClubExcelService 클래스의") {
            describe("execute 메서드는") {

                context("정상적인 Excel 파일로 동아리를 생성/수정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val leader1 =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "kim@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    val leader2 =
                        StudentJpaEntity().apply {
                            id = 2L
                            name = "이영희"
                            studentNumber = StudentNumber(2, 3, 5)
                            email = "lee@gsm.hs.kr"
                            major = Major.AI
                            sex = Sex.WOMAN
                        }

                    val leader3 =
                        StudentJpaEntity().apply {
                            id = 3L
                            name = "박민수"
                            studentNumber = StudentNumber(1, 2, 10)
                            email = "park@gsm.hs.kr"
                            major = Major.SMART_IOT
                            sex = Sex.MAN
                        }

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    2,
                                    4,
                                    4,
                                    "김철수",
                                )
                        } returns leader1
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    2,
                                    3,
                                    5,
                                    "이영희",
                                )
                        } returns leader2
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    1,
                                    2,
                                    10,
                                    "박민수",
                                )
                        } returns leader3
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                    }

                    it("동아리를 저장하고 성공 메시지를 반환해야 한다") {
                        val result = modifyClubExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"
                        result.code shouldBe HttpStatus.OK.value()

                        val clubsSlot = slot<List<ClubJpaEntity>>()
                        verify(exactly = 1) { mockClubRepository.saveAll(capture(clubsSlot)) }

                        val savedClubs = clubsSlot.captured
                        savedClubs.size shouldBe 3
                        savedClubs[0].name shouldBe "SW개발동아리"
                        savedClubs[0].type shouldBe ClubType.MAJOR_CLUB
                        savedClubs[0].leader.name shouldBe "김철수"
                    }
                }

                context("지원하지 않는 파일 형식일 때") {
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.pdf",
                            "application/pdf",
                            "dummy pdf content".toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldBe "지원하지 않는 파일 형식입니다. (xlsx, xls만 지원)"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("중복된 동아리명이 존재할 때") {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("전공동아리")
                    headerRow.createCell(1).setCellValue("전공동아리 부장")
                    headerRow.createCell(2).setCellValue("취업동아리")
                    headerRow.createCell(3).setCellValue("취업동아리 부장")
                    headerRow.createCell(4).setCellValue("창체동아리")
                    headerRow.createCell(5).setCellValue("창체동아리 부장")

                    val row1 = sheet.createRow(1)
                    row1.createCell(0).setCellValue("중복동아리")
                    row1.createCell(1).setCellValue("2404 김철수")

                    val row2 = sheet.createRow(2)
                    row2.createCell(2).setCellValue("중복동아리")
                    row2.createCell(3).setCellValue("2305 이영희")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "엑셀 파일에 다음 동아리가 중복으로 존재합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("모든 동아리명이 비어있을 때") {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("전공동아리")
                    headerRow.createCell(1).setCellValue("전공동아리 부장")
                    headerRow.createCell(2).setCellValue("취업동아리")
                    headerRow.createCell(3).setCellValue("취업동아리 부장")
                    headerRow.createCell(4).setCellValue("창체동아리")
                    headerRow.createCell(5).setCellValue("창체동아리 부장")

                    val row1 = sheet.createRow(1)
                    row1.createCell(0).setCellValue("")
                    row1.createCell(1).setCellValue("")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldBe "엑셀 내 모든 동아리명이 비어있습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("잘못된 헤더 형식일 때") {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("잘못된헤더")
                    headerRow.createCell(1).setCellValue("전공동아리 부장")
                    headerRow.createCell(2).setCellValue("취업동아리")
                    headerRow.createCell(3).setCellValue("취업동아리 부장")
                    headerRow.createCell(4).setCellValue("창체동아리")
                    headerRow.createCell(5).setCellValue("창체동아리 부장")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "헤더 행의 열은 순서대로"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("부장 정보가 비어있을 때") {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("전공동아리")
                    headerRow.createCell(1).setCellValue("전공동아리 부장")
                    headerRow.createCell(2).setCellValue("취업동아리")
                    headerRow.createCell(3).setCellValue("취업동아리 부장")
                    headerRow.createCell(4).setCellValue("창체동아리")
                    headerRow.createCell(5).setCellValue("창체동아리 부장")

                    val row1 = sheet.createRow(1)
                    row1.createCell(0).setCellValue("SW개발동아리")
                    row1.createCell(1).setCellValue("")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldBe "동아리 부장 정보가 비어있습니다."
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("부장 정보 형식이 올바르지 않을 때 (스페이스 누락)") {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("전공동아리")
                    headerRow.createCell(1).setCellValue("전공동아리 부장")
                    headerRow.createCell(2).setCellValue("취업동아리")
                    headerRow.createCell(3).setCellValue("취업동아리 부장")
                    headerRow.createCell(4).setCellValue("창체동아리")
                    headerRow.createCell(5).setCellValue("창체동아리 부장")

                    val row1 = sheet.createRow(1)
                    row1.createCell(0).setCellValue("SW개발동아리")
                    row1.createCell(1).setCellValue("2404김철수")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "동아리 부장 정보 형식이 올바르지 않습니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("학번이 4자리가 아닐 때") {
                    val workbook = XSSFWorkbook()
                    val sheet = workbook.createSheet("동아리")
                    val headerRow = sheet.createRow(0)
                    headerRow.createCell(0).setCellValue("전공동아리")
                    headerRow.createCell(1).setCellValue("전공동아리 부장")
                    headerRow.createCell(2).setCellValue("취업동아리")
                    headerRow.createCell(3).setCellValue("취업동아리 부장")
                    headerRow.createCell(4).setCellValue("창체동아리")
                    headerRow.createCell(5).setCellValue("창체동아리 부장")

                    val row1 = sheet.createRow(1)
                    row1.createCell(0).setCellValue("SW개발동아리")
                    row1.createCell(1).setCellValue("240 김철수")

                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    workbook.close()

                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            output.toByteArray(),
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "학번은 4자리 숫자여야 합니다"
                        exception.statusCode shouldBe HttpStatus.BAD_REQUEST
                    }
                }

                context("존재하지 않는 학생으로 부장을 지정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns emptyList()
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                )
                        } returns null
                    }

                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                modifyClubExcelService.execute(file)
                            }

                        exception.message shouldContain "에 해당하는 학생을 찾을 수 없습니다"
                        exception.statusCode shouldBe HttpStatus.NOT_FOUND
                    }
                }

                context("기존 동아리를 수정할 때") {
                    val excelBytes = createValidExcelFile()
                    val file =
                        MockMultipartFile(
                            "file",
                            "clubs.xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            excelBytes,
                        )

                    val existingClub =
                        ClubJpaEntity().apply {
                            id = 100L
                            name = "SW개발동아리"
                            type = ClubType.MAJOR_CLUB
                        }

                    val newLeader =
                        StudentJpaEntity().apply {
                            id = 1L
                            name = "김철수"
                            studentNumber = StudentNumber(2, 4, 4)
                            email = "kim@gsm.hs.kr"
                            major = Major.SW_DEVELOPMENT
                            sex = Sex.MAN
                        }

                    beforeEach {
                        every { mockClubRepository.findAllByNameIn(any()) } returns listOf(existingClub)
                        every {
                            mockStudentRepository
                                .findByStudentNumberStudentGradeAndStudentNumberStudentClassAndStudentNumberStudentNumberAndName(
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                )
                        } returns newLeader
                        every { mockClubRepository.saveAll(any<List<ClubJpaEntity>>()) } returns emptyList()
                    }

                    it("기존 동아리를 수정해야 한다") {
                        val result = modifyClubExcelService.execute(file)

                        result.message shouldBe "엑셀 업로드 성공"

                        val clubsSlot = slot<List<ClubJpaEntity>>()
                        verify(exactly = 1) { mockClubRepository.saveAll(capture(clubsSlot)) }

                        val savedClubs = clubsSlot.captured
                        savedClubs.any { it.id == 100L } shouldBe true
                    }
                }
            }
        }
    })
