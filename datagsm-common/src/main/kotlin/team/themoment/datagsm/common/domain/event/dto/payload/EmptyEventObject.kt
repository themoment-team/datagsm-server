package team.themoment.datagsm.common.domain.event.dto.payload

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 생성 이벤트의 old, 삭제 이벤트의 new 에서 사용하는 빈 객체.
 * JSON 직렬화 시 `{}` 로 표현되어 해당 시점에 데이터가 없음을 나타낸다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class EmptyEventObject
