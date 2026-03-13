package team.themoment.datagsm.web.domain.application.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.entity.constant.AccountRole
import team.themoment.datagsm.common.domain.application.repository.ApplicationJpaRepository
import team.themoment.datagsm.web.domain.application.service.DeleteApplicationService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteApplicationServiceImpl(
    private val applicationJpaRepository: ApplicationJpaRepository,
    private val currentUserProvider: CurrentUserProvider,
) : DeleteApplicationService {
    @Transactional
    override fun execute(id: String) {
        val application =
            applicationJpaRepository.findById(id).orElseThrow {
                ExpectedException("Application을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            }

        val currentAccount = currentUserProvider.getCurrentAccount()
        val isAdmin = currentAccount.role == AccountRole.ADMIN || currentAccount.role == AccountRole.ROOT

        if (application.account != currentAccount && !isAdmin) {
            throw ExpectedException("Application 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN)
        }

        applicationJpaRepository.delete(application)
    }
}
