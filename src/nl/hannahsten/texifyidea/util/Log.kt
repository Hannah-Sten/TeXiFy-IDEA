package nl.hannahsten.texifyidea.util

import com.intellij.openapi.diagnostic.Logger

/**
 * Log messages to idea.log. During plugin development, it logs to build/idea-sandbox/system/log/idea.log.
 * @author Hannah Schellekens
 */
object Log {

    private val logger: Logger by lazy { Logger.getInstance(Log::class.java) }

    private const val prefix = "TEXIFY-IDEA -"

    /**
     * Sends an info message to the IntelliJ logger.
     *
     * All messages start with the prefix `TEXIFY-IDEA - `.
     */
    fun info(message: String) = logger.info("$prefix $message")

    fun warn(message: String) = logger.warn("$prefix $message")

    fun debug(message: String) = logger.debug("$prefix $message")
}