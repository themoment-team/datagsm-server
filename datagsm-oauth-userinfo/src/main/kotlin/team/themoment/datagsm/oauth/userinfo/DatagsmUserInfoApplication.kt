package team.themoment.datagsm.oauth.userinfo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.oauth.userinfo", "team.themoment.datagsm.common"],
)
class DatagsmOauthUserInfoApplication

fun main(args: Array<String>) {
    runApplication<DatagsmOauthUserInfoApplication>(*args)
}
