package latipe.notification.configs

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import java.io.IOException
import java.net.ServerSocket


class PortInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PortInitializer::class.java)
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val environment = applicationContext.environment
        val port = environment.getProperty("server.port", Int::class.java) ?: findRandomPort()
        if (isPortInUse(port)) {
            val randomPort = findRandomPort()
            LOGGER.info("[Notification Service]: Port $port is already in use, so using random port $randomPort")
            System.setProperty("server.port", randomPort.toString())
            return
        }
        LOGGER.info("[Notification Service]: Port $port is available")
    }

    private fun isPortInUse(port: Int): Boolean {
        return try {
            ServerSocket(port).use { false }
        } catch (e: IOException) {
            true
        }
    }

    private fun findRandomPort(): Int {
        return try {
            ServerSocket(0).use { it.localPort }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}