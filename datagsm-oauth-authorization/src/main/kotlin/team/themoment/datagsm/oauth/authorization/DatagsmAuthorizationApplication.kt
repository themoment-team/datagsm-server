package team.themoment.datagsm.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.authorization", "team.themoment.datagsm.common"],
)
class DatagsmAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<DatagsmAuthorizationApplication>(*args)
}
