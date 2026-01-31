package team.themoment.datagsm.openapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.openapi", "team.themoment.datagsm.common"],
)
class DatagsmOpenApiApplication

fun main(args: Array<String>) {
    runApplication<DatagsmOpenApiApplication>(*args)
}
