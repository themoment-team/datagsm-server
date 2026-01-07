package team.themoment.datagsm.authorization.global.thirdparty.feign.resource.dto

data class StudentResDto(
    val id: Long,
    val name: String,
    val email: String,
    val grade: Int,
    val classNum: Int,
    val number: Int,
)
