package team.themoment.datagsm.web.domain.account.service.impl

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.themoment.datagsm.common.domain.account.dto.request.QueryAccountReqDto
import team.themoment.datagsm.common.domain.account.dto.response.AccountListResDto
import team.themoment.datagsm.common.domain.account.dto.response.AccountResDto
import team.themoment.datagsm.common.domain.account.repository.AccountJpaRepository
import team.themoment.datagsm.web.domain.account.service.QueryAccountService

@Service
class QueryAccountServiceImpl(
    private val accountJpaRepository: AccountJpaRepository,
) : QueryAccountService {
    @Transactional(readOnly = true)
    override fun execute(queryReq: QueryAccountReqDto): AccountListResDto {
        val accountPage =
            accountJpaRepository.searchAccountsWithPaging(
                email = queryReq.email,
                role = queryReq.role,
                isStudent = queryReq.isStudent,
                pageable = PageRequest.of(queryReq.page, queryReq.size),
                sortBy = queryReq.sortBy,
                sortDirection = queryReq.sortDirection,
            )

        return AccountListResDto(
            totalElements = accountPage.totalElements,
            totalPages = accountPage.totalPages,
            accounts = accountPage.content.map { AccountResDto.from(it) },
        )
    }
}
