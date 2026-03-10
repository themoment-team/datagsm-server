package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.request.DeleteMyAccountReqDto
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.common.domain.auth.repository.ApiKeyJpaRepository
import team.themoment.datagsm.common.domain.client.repository.ClientJpaRepository
import team.themoment.datagsm.web.domain.account.service.DeleteMyAccountService
import team.themoment.datagsm.web.global.security.provider.CurrentUserProvider
import team.themoment.sdk.exception.ExpectedException

@Service
class DeleteMyAccountServiceImpl(
    private val currentUserProvider: CurrentUserProvider,
    private val passwordEncoder: PasswordEncoder,
    private val apiKeyJpaRepository: ApiKeyJpaRepository,
    private val clientJpaRepository: ClientJpaRepository,
    private val accountJpaRepository: AccountJpaRepository,
) : DeleteMyAccountService {
    @Transactional
    override fun execute(reqDto: DeleteMyAccountReqDto) {
        val account = currentUserProvider.getCurrentAccount()

        if (!passwordEncoder.matches(reqDto.password, account.password)) {
            throw ExpectedException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED)
        }

        apiKeyJpaRepository.deleteByAccount(account)
        clientJpaRepository.deleteAllByAccount(account)
        accountJpaRepository.delete(account)
    }
}
