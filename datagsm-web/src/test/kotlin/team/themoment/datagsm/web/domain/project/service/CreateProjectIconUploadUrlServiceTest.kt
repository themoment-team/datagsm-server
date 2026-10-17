package team.themoment.datagsm.web.domain.project.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.HttpStatus
import team.themoment.datagsm.common.domain.project.dto.request.CreateProjectIconUploadUrlReqDto
import team.themoment.datagsm.web.domain.project.service.impl.CreateProjectIconUploadUrlServiceImpl
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.datagsm.web.global.security.service.IconUploadRateLimitService
import team.themoment.datagsm.web.global.storage.ProjectIconStorage
import team.themoment.datagsm.web.global.storage.ProjectIconUploadTarget
import team.themoment.sdk.exception.ExpectedException

class CreateProjectIconUploadUrlServiceTest :
    DescribeSpec({

        val mockIconStorage = mockk<ProjectIconStorage>()
        val mockRateLimitService = mockk<IconUploadRateLimitService>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val service =
            CreateProjectIconUploadUrlServiceImpl(mockIconStorage, mockRateLimitService, mockCurrentUserProvider)

        val reqDto = CreateProjectIconUploadUrlReqDto(contentType = "image/png", contentLength = 1024)

        beforeEach {
            every { mockCurrentUserProvider.getCurrentUserEmail() } returns "s24080@gsm.hs.kr"
        }

        afterEach {
            clearAllMocks()
        }

        describe("CreateProjectIconUploadUrlService 클래스의") {
            describe("execute 메서드는") {

                context("요청 제한에 걸리지 않은 경우") {
                    beforeEach {
                        justRun { mockRateLimitService.ensureNotExceeded("s24080@gsm.hs.kr") }
                        every { mockIconStorage.createUploadUrl("image/png", 1024) } returns
                            ProjectIconUploadTarget(
                                uploadUrl = "https://s3.example.com/signed",
                                iconKey = "project-icons/abc.png",
                                expiresInSeconds = 300,
                            )
                    }

                    it("업로드 URL과 아이콘 키가 반환되어야 한다") {
                        val result = service.execute(reqDto)

                        result.uploadUrl shouldBe "https://s3.example.com/signed"
                        result.iconKey shouldBe "project-icons/abc.png"
                        result.expiresInSeconds shouldBe 300
                    }
                }

                context("요청 제한을 초과한 경우") {
                    beforeEach {
                        every { mockRateLimitService.ensureNotExceeded("s24080@gsm.hs.kr") } throws
                            ExpectedException(
                                "아이콘 업로드 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.",
                                HttpStatus.TOO_MANY_REQUESTS,
                            )
                    }

                    it("presigned URL을 발급하지 않고 예외가 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                service.execute(reqDto)
                            }

                        exception.message shouldBe "아이콘 업로드 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
                        verify(exactly = 0) { mockIconStorage.createUploadUrl(any(), any()) }
                    }
                }
            }
        }
    })
