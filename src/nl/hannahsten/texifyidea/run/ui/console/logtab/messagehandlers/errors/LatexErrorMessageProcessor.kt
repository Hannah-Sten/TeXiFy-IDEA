package nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.errors

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex

abstract class LatexErrorMessageProcessor(vararg val regex: Regex) {

    abstract fun process(message: String): String?
    fun postProcess(message: String?): String? = message
        ?.replace("See the LaTeX manual or LaTeX Companion for explanation.", "")
        ?.trim()
}

/**
 * LaTeX Error: text -> text
 */
object LatexRemoveErrorTextProcessor : LatexErrorMessageProcessor("""LaTeX Error:""".toRegex(), """pdfTeX error:""".toRegex()) {

    override fun process(message: String): String? {
        regex.forEach {
            if (it.containsMatchIn(message)) return it.replace(message, "").trim()
        }
        return null
    }
}

/**
 * Package amsmath error: text -> amsmath: text
 */
object LatexPackageErrorProcessor : LatexErrorMessageProcessor("""^Package ${LatexLogMagicRegex.PACKAGE_REGEX} Error:""".toRegex()) {

    override fun process(message: String): String? {
        regex.forEach {
            @Suppress("ktlint:standard:property-naming")
            val `package` = it.find(message)?.groups?.get("package")?.value ?: return@forEach
            return "${`package`}: ${it.replace(message, "").trim()}"
        }
        return null
    }
}