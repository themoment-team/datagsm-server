package team.themoment.datagsm.resource

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@EnableFeignClients(basePackages = ["team.themoment.datagsm"])
@EnableJpaRepositories(basePackages = ["team.themoment.datagsm"])
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["team.themoment.datagsm"])
class DatagsmResourceApplication

fun main(args: Array<String>) {
    runApplication<DatagsmResourceApplication>(*args)
}
