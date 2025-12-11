package team.themoment.datagsm.domain.client.service

import team.themoment.datagsm.domain.client.dto.req.DeleteClientReqDto

interface DeleteClientService {
    fun execute(reqDto: DeleteClientReqDto)
}
