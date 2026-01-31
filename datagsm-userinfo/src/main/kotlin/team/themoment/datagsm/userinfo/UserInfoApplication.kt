package team.themoment.datagsm.userinfo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.userinfo", "team.themoment.datagsm.common"],
)
class UserInfoApplication

fun main(args: Array<String>) {
    runApplication<UserInfoApplication>(*args)
}
