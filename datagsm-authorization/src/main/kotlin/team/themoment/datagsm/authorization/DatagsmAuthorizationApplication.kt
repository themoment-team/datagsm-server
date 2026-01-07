package team.themoment.datagsm.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableFeignClients(basePackages = ["team.themoment.datagsm"])
@EnableJpaRepositories(basePackages = ["team.themoment.datagsm"])
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
@SpringBootApplication(scanBasePackages = ["team.themoment.datagsm"])
class DatagsmAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<DatagsmAuthorizationApplication>(*args)
}
