package nl.hannahsten.texifyidea.util

/**
 * @author Hannah Schellekens
 */
object Clipboard {

    /**
     * Takes the complete clipboard contents (must have an html data flavor) and extracts the html (thus dropping
     * the header).
     *
     * @return null when it could not find html
     */
    @JvmStatic
    fun extractHtmlFromClipboard(clipboardContents: String): String? {
        return clipboardContents.indexOf("<html", ignoreCase = true)
                .takeIf { it >= 0 }
                ?.let { clipboardContents.substring(it) }
    }
}