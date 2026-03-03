package team.themoment.datagsm.common.global.banner
// team.themoment.datagsm.common.global.common.banner로 가는게 맞을듯? 아닌가?

import org.springframework.boot.Banner
import org.springframework.boot.ResourceBanner
import org.springframework.boot.ansi.AnsiColor
import org.springframework.boot.ansi.AnsiOutput
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import java.io.PrintStream
import java.util.Properties

class GitAwareBanner : Banner {

    override fun printBanner(environment: Environment, sourceClass: Class<*>?, out: PrintStream) {
        val bannerResource = ClassPathResource("banner.txt")
        if (bannerResource.exists()) {
            ResourceBanner(bannerResource).printBanner(environment, sourceClass, out)
        }

        val gitResource = ClassPathResource("git.properties")
        if (!gitResource.exists()) return

        val props = Properties()
        gitResource.inputStream.use { props.load(it) }

        val branch = props.getProperty("git.branch", "?")
        val commitId = props.getProperty("git.commit.id.abbrev", "?")
        val commitTime = props.getProperty("git.commit.time", "?")
        val message = props.getProperty("git.commit.message.short", "?").trim()

        out.println(
            "${AnsiOutput.encode(AnsiColor.WHITE)}   Git: $branch @ $commitId | $commitTime | $message${AnsiOutput.encode(AnsiColor.DEFAULT)}"
        )
    }
}
