package team.themoment.datagsm.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@EnableFeignClients(basePackages = ["team.themoment.datagsm"])
@EnableJpaRepositories(basePackages = ["team.themoment.datagsm"])
@EnableRedisRepositories(basePackages = ["team.themoment.datagsm"])
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
@SpringBootApplication(scanBasePackages = ["team.themoment.datagsm"])
class DatagsmAuthorizationApplication

fun main(args: Array<String>) {
    runApplication<DatagsmAuthorizationApplication>(*args)
}
