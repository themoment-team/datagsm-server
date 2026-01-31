package team.themoment.datagsm.oauth.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.oauth.authorization", "team.themoment.datagsm.common"],
)
class DatagsmOauthAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<DatagsmOauthAuthorizationApplication>(*args)
}
