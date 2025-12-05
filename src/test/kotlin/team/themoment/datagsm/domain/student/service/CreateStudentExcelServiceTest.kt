package team.themoment.datagsm.domain.student.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import team.themoment.datagsm.domain.student.dto.internal.ExcelColumnDto
import team.themoment.datagsm.domain.student.dto.internal.ExcelRowDto
import team.themoment.datagsm.domain.student.repository.StudentJpaRepository
import team.themoment.datagsm.domain.student.service.impl.CreateStudentExcelServiceImpl
import java.io.ByteArrayInputStream
import java.io.File

class CreateStudentExcelServiceTest : DescribeSpec({
    val mockRepository = mockk<StudentJpaRepository>()

    val createExcelService = CreateStudentExcelServiceImpl(mockRepository)

    describe("엑셀 파일을 만들려고 한다") {
        context("유효한 학생 데이터가 주어졌을 때") {
            val testData = listOf<ExcelRowDto>(
                ExcelRowDto(
                    excelRows = listOf(
                        ExcelColumnDto(
                            "홍길동", 1112, "test@gmail.com", "SW개발",
                            "더모먼트", "백엔드2", "블랜드",
                            301, "일반인", false, "남자"
                        ),
                        ExcelColumnDto(
                            "김학생", 1113, "test@gmail.com", "SW개발",
                            "더모먼트", "백엔드2", "블랜드",
                            301, "학생회", false, "남자"
                        ),
                        ExcelColumnDto(
                            "자퇴자", 1114, "test@gmail.com", "SW개발",
                            "더모먼트", "백엔드2", "블랜드",
                            301, "일반인", true, "남자"
                        ),
                    )

                )
            )

            lateinit var excelBytes: ByteArray

            beforeEach {
                excelBytes = createExcelService.createExcel(testData)
            }

            it("엑셀 ByteArray가 생성되어야 한다") {
                excelBytes.size shouldBeGreaterThan 0
            }

            it("파일이 xlsx 포맷이여야 한다") {
                excelBytes.copyOfRange(0, 4).toHexString() shouldBe "504b0304"
            }

            it("엑셀 첫 번째 시트에 홍길동 학생 정보가 있어야 한다") {
                val workbook = WorkbookFactory.create(ByteArrayInputStream(excelBytes))
                val sheet = workbook.getSheetAt(0)

                sheet.getRow(1).getCell(0).stringCellValue shouldBe "홍길동"

                workbook.close()
            }

            it("엑셀 파일을 실제로 저장해본다") {
                val outputPath = "build/generated-test-files/excel_test_output.xlsx"
                File(outputPath).writeBytes(excelBytes)

                excelBytes.size shouldBeGreaterThan 0
            }
        }
    }
})
