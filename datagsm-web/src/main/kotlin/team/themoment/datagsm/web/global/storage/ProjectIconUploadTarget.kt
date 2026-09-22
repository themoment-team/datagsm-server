package team.themoment.datagsm.web.global.storage

data class ProjectIconUploadTarget(
    val uploadUrl: String,
    val iconKey: String,
    val expiresInSeconds: Long,
)
