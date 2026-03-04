package team.themoment.datagsm.oauth.userinfo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import team.themoment.datagsm.common.global.common.banner.GitAwareBanner

@SpringBootApplication(
    scanBasePackages = ["team.themoment.datagsm.oauth.userinfo", "team.themoment.datagsm.common"],
)
class DatagsmOauthUserInfoApplication

fun main(args: Array<String>) {
    runApplication<DatagsmOauthUserInfoApplication>(*args) {
        setBanner(GitAwareBanner())
        setDefaultProperties(mapOf("spring.banner.location" to "classpath:_"))
    }
}
