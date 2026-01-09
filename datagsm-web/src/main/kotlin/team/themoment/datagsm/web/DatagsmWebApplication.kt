package team.themoment.datagsm.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["team.themoment.datagsm"])
@EnableScheduling
@EntityScan(basePackages = ["team.themoment.datagsm.common.domain"])
@EnableJpaRepositories(basePackages = ["team.themoment.datagsm"])
@EnableRedisRepositories(basePackages = ["team.themoment.datagsm"])
class DatagsmWebApplication

fun main(args: Array<String>) {
    runApplication<DatagsmWebApplication>(*args)
}
