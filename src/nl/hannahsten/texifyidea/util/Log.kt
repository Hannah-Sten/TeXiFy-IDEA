package nl.hannahsten.texifyidea.util

import com.intellij.openapi.diagnostic.Logger

/**
 * Log messages to idea.log. During plugin development, it logs to build/idea-sandbox/system/log/idea.log.
 * @author Hannah Schellekens
 */
object Log {

    private val logger: Logger by lazy { Logger.getInstance(Log::class.java) }

    /**
     * Sends an info message to the IntelliJ logger.
     *
     * All messages start with the prefix `TEXIFY-IDEA - `.
     */
    @JvmStatic
    fun logf(message: String) = logger.info("TEXIFY-IDEA - $message")
}