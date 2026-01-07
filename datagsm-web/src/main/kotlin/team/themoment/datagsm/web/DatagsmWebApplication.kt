package team.themoment.datagsm.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
@EnableJpaRepositories(basePackages = ["team.themoment.datagsm.web.domain"])
class DatagsmWebApplication

fun main(args: Array<String>) {
    runApplication<DatagsmWebApplication>(*args)
}
