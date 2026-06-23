package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonInclude

// webhook 전송용 payload: 생성의 old, 삭제의 new 에 쓰는 빈 객체. 직렬화 시 {} 로 표현
@JsonInclude(JsonInclude.Include.NON_NULL)
class EmptyEventObject
