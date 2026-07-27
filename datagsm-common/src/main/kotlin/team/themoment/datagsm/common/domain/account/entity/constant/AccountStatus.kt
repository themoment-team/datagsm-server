package team.themoment.datagsm.common.domain.account.entity.constant

/**
 * 계정의 활성화 상태를 정의하는 열거형 클래스입니다.
 * 선생님 계정은 어드민 승인 전까지 PENDING 상태로 유지됩니다.
 */
enum class AccountStatus {
    PENDING,
    ACTIVE,
}
