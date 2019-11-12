package nl.hannahsten.texifyidea.util

import com.intellij.openapi.diagnostic.Logger

/**
 * @author Hannah Schellekens
 */
object Log {

    private val logger: Logger by lazy { Logger.getInstance(Log::class.java) }

    /**
     * Sends a formatted info message to the IntelliJ logger.
     *
     * All messages start with the prefix `TEXIFY-IDEA - `.
     *
     * @param format
     *         How the log should be formatted, see also [String.format].
     * @param objects
     *         The objects to bind to the format.
     */
    @JvmStatic
    fun logf(format: String, vararg objects: Any?) = logger.info("TEXIFY-IDEA - " + format.format(*objects))
}