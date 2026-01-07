package team.themoment.datagsm.web.global.common.logging.wrapper

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class CachedRequestBodyWrapper
    @Throws(IOException::class)
    constructor(
        request: HttpServletRequest,
    ) : HttpServletRequestWrapper(request) {
        val cachedBody: ByteArray = StreamUtils.copyToByteArray(request.inputStream)

        override fun getInputStream(): ServletInputStream {
            val byteArrayInputStream = ByteArrayInputStream(cachedBody)
            return object : ServletInputStream() {
                override fun read(): Int = byteArrayInputStream.read()

                override fun isFinished(): Boolean = byteArrayInputStream.available() == 0

                override fun isReady(): Boolean = true

                override fun setReadListener(readListener: ReadListener?) {
                }
            }
        }

        override fun getReader(): BufferedReader {
            val charset = characterEncoding?.let { Charset.forName(it) } ?: StandardCharsets.UTF_8
            return BufferedReader(InputStreamReader(inputStream, charset))
        }
    }
