package team.themoment.datagsm.web.global.storage

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.mockk.every
import io.mockk.mockk
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import team.themoment.datagsm.common.global.data.ProjectIconStorageEnvironment
import team.themoment.datagsm.common.global.storage.ProjectIconUrlResolver
import team.themoment.sdk.exception.ExpectedException
import java.net.URI

class ProjectIconStorageTest :
    DescribeSpec({

        val mockPresigner = mockk<S3Presigner>()

        val environment =
            ProjectIconStorageEnvironment(
                bucket = "datagsm-assets",
                region = "ap-northeast-2",
                cdnBaseUrl = "https://cdn.datagsm.kr",
            )

        val projectIconStorage =
            ProjectIconStorage(mockPresigner, environment, ProjectIconUrlResolver(environment))

        describe("ProjectIconStorage 클래스의") {

            describe("createUploadUrl 메서드는") {

                beforeEach {
                    val presignedRequest = mockk<PresignedPutObjectRequest>()
                    every { presignedRequest.url() } returns URI("https://s3.example.com/signed").toURL()
                    every {
                        mockPresigner.presignPutObject(any<software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest>())
                    } returns
                        presignedRequest
                }

                context("허용된 이미지 형식이 주어질 때") {
                    it("서버가 생성한 UUID 키와 업로드 URL을 반환해야 한다") {
                        val result = projectIconStorage.createUploadUrl("image/png", 1024)

                        result.iconKey shouldStartWith "project-icons/"
                        result.iconKey shouldEndWith ".png"
                        result.uploadUrl shouldBe "https://s3.example.com/signed"
                        result.expiresInSeconds shouldBe 300
                    }
                }

                context("허용되지 않은 형식이 주어질 때") {
                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                projectIconStorage.createUploadUrl("image/svg+xml", 1024)
                            }

                        exception.message shouldBe "지원하지 않는 이미지 형식입니다."
                    }
                }

                context("허용 크기를 초과할 때") {
                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                projectIconStorage.createUploadUrl("image/png", 6 * 1024 * 1024)
                            }

                        exception.message shouldBe "이미지 크기가 허용 범위를 초과했습니다."
                    }
                }
            }

            describe("validateIconKey 메서드는") {

                context("서버가 발급한 형식의 키가 주어질 때") {
                    it("키를 그대로 반환해야 한다") {
                        val iconKey = "project-icons/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png"

                        projectIconStorage.validateIconKey(iconKey) shouldBe iconKey
                    }
                }

                context("키가 비어 있을 때") {
                    it("null을 반환해야 한다") {
                        projectIconStorage.validateIconKey(null) shouldBe null
                        projectIconStorage.validateIconKey("") shouldBe null
                    }
                }

                context("경로 상위 이동이 포함된 키가 주어질 때") {
                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                projectIconStorage.validateIconKey("../secrets/key.png")
                            }

                        exception.message shouldBe "올바르지 않은 아이콘 키입니다."
                    }
                }

                context("허용되지 않은 프리픽스의 키가 주어질 때") {
                    it("ExpectedException이 발생해야 한다") {
                        val exception =
                            shouldThrow<ExpectedException> {
                                projectIconStorage.validateIconKey("private/3f2504e0-4f89-11d3-9a0c-0305e82c3301.png")
                            }

                        exception.message shouldBe "올바르지 않은 아이콘 키입니다."
                    }
                }
            }

            describe("toIconUrl 메서드는") {

                context("키가 주어질 때") {
                    it("CDN 도메인이 붙은 URL을 반환해야 한다") {
                        val iconUrl = projectIconStorage.toIconUrl("project-icons/abc.png")

                        iconUrl shouldBe "https://cdn.datagsm.kr/project-icons/abc.png"
                    }
                }

                context("키가 null일 때") {
                    it("null을 반환해야 한다") {
                        projectIconStorage.toIconUrl(null) shouldBe null
                    }
                }
            }
        }
    })
