package team.themoment.datagsm.oauth.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import team.themoment.datagsm.common.global.banner.GitAwareBanner

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.oauth.authorization", "team.themoment.datagsm.common"],
)
class DatagsmOauthAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<DatagsmOauthAuthorizationApplication>(*args) {
        setBanner(GitAwareBanner())
    }
}
