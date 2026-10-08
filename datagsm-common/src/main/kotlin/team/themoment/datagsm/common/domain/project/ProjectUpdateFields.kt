package team.themoment.datagsm.common.domain.project

import team.themoment.datagsm.common.domain.project.entity.constant.ProjectCategory

/**
 * 수정 요청에서 생략(null)하면 현재 값을 유지하고, 빈 문자열이면 삭제로 간주한다.
 * 값을 매번 다시 보내지 않아도 되도록 하되 지우는 수단은 남겨 둔다.
 */
fun resolveDeploymentUrlForUpdate(
    requestedDeploymentUrl: String?,
    currentDeploymentUrl: String?,
): String? =
    when {
        requestedDeploymentUrl == null -> currentDeploymentUrl
        requestedDeploymentUrl.isBlank() -> null
        else -> requestedDeploymentUrl
    }

/** 수정 요청에서 생략(null)하면 현재 카테고리를 유지한다. */
fun resolveCategoryForUpdate(
    requestedCategory: ProjectCategory?,
    currentCategory: ProjectCategory?,
): ProjectCategory? = requestedCategory ?: currentCategory
