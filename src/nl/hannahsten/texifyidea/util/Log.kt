package nl.hannahsten.texifyidea.util

import com.intellij.openapi.diagnostic.Logger

/**
 * Log messages to idea.log. During plugin development, it logs to build/idea-sandbox/system/log/idea.log.
 * You can add a tab to the run configuration tool window by adding 'Specify logs to be shown in console' to the runIde run configuration.
 * To view debug logs, in the dev instance go to Help > Diagnostic Tools > Debug Log Settings and add #nl.hannahsten.texifyidea.util.Log.
 * Then in the console, make sure the filter is set to All instead of Warning.
 *
 * @author Hannah Schellekens
 */
object Log {

    private val logger: Logger by lazy { Logger.getInstance(Log::class.java) }

    private const val PREFIX = "TEXIFY-IDEA -"

    /**
     * Sends an info message to the IntelliJ logger.
     *
     * All messages start with the prefix `TEXIFY-IDEA - `.
     */
    fun info(message: String) = logger.info("$PREFIX $message")

    fun warn(message: String) = logger.warn("$PREFIX $message")

    fun debug(message: String) = logger.debug("$PREFIX $message")

    fun error(message: String) = logger.error("$PREFIX $message")
}