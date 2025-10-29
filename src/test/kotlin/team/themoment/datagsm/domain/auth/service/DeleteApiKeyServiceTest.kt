package team.themoment.datagsm.domain.auth.service

import io.kotest.core.spec.style.DescribeSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import team.themoment.datagsm.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.domain.auth.service.impl.DeleteApiKeyServiceImpl
import team.themoment.datagsm.domain.student.entity.StudentJpaEntity
import team.themoment.datagsm.global.security.provider.CurrentUserProvider

class DeleteApiKeyServiceTest :
    DescribeSpec({

        val mockApiKeyRepository = mockk<ApiKeyJpaRepository>()
        val mockCurrentUserProvider = mockk<CurrentUserProvider>()

        val deleteApiKeyService = DeleteApiKeyServiceImpl(mockApiKeyRepository, mockCurrentUserProvider)

        afterEach {
            clearAllMocks()
        }

        describe("DeleteApiKeyService 클래스의") {
            describe("execute 메서드는") {

                val mockStudent =
                    StudentJpaEntity().apply {
                        studentId = 1L
                        studentEmail = "test@gsm.hs.kr"
                    }

                beforeEach {
                    every { mockCurrentUserProvider.getCurrentStudent() } returns mockStudent
                }

                context("정상적으로 API 키를 삭제할 때") {
                    beforeEach {
                        every { mockApiKeyRepository.deleteByApiKeyStudent(mockStudent) } returns Unit
                    }

                    it("현재 학생의 API 키를 삭제해야 한다") {
                        deleteApiKeyService.execute()

                        verify(exactly = 1) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 1) { mockApiKeyRepository.deleteByApiKeyStudent(mockStudent) }
                    }
                }

                context("여러 번 호출될 때") {
                    beforeEach {
                        every { mockApiKeyRepository.deleteByApiKeyStudent(mockStudent) } returns Unit
                    }

                    it("매번 삭제 작업이 수행되어야 한다") {
                        deleteApiKeyService.execute()
                        deleteApiKeyService.execute()
                        deleteApiKeyService.execute()

                        verify(exactly = 3) { mockCurrentUserProvider.getCurrentStudent() }
                        verify(exactly = 3) { mockApiKeyRepository.deleteByApiKeyStudent(mockStudent) }
                    }
                }
            }
        }
    })
