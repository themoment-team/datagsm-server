package team.themoment.datagsm.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.web", "team.themoment.datagsm.common"],
)
class DatagsmWebApplication

fun main(args: Array<String>) {
    runApplication<DatagsmWebApplication>(*args)
}
