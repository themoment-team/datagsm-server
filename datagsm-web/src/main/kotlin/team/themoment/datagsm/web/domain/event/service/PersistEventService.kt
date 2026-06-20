package team.themoment.datagsm.web.domain.event.service

import team.themoment.datagsm.common.domain.account.entity.AccountJpaEntity
import team.themoment.datagsm.common.domain.event.dto.request.CreateEventReqDto
import team.themoment.datagsm.common.domain.event.dto.request.ModifyEventReqDto
import team.themoment.datagsm.common.domain.event.dto.response.CreateEventResDto
import team.themoment.datagsm.common.domain.event.dto.response.EventResDto

interface PersistEventService {
    fun persistCreate(
        account: AccountJpaEntity,
        reqDto: CreateEventReqDto,
        secret: String,
    ): CreateEventResDto

    fun persistModify(
        account: AccountJpaEntity,
        eventId: Long,
        reqDto: ModifyEventReqDto,
    ): EventResDto
}
