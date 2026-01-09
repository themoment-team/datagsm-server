package team.themoment.datagsm.resource

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.resource", "team.themoment.datagsm.common"],
)
class DatagsmResourceApplication

fun main(args: Array<String>) {
    runApplication<DatagsmResourceApplication>(*args)
}
