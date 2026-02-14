package team.themoment.datagsm.oauth.authorization.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer

@Configuration
class SessionCookieConfig {
    @Bean
    fun cookieSerializer(): CookieSerializer =
        DefaultCookieSerializer().apply {
            setCookieName("SESSION")
            setCookiePath("/")
            setUseHttpOnlyCookie(true)
            setUseSecureCookie(false)
            setSameSite(null)
            setCookieMaxAge(600)
        }
}
