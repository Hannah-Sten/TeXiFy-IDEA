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
    fun extractHtmlFromClipboard(clipboardContents: String): String = clipboardContents.indexOf("<html", ignoreCase = true)
        .coerceAtLeast(0)
        .let { clipboardContents.substring(it) }

    /**
     * Extracts the fragment html from the given contents.
     *
     * @return null when it could not find the fragment.
     */
    @JvmStatic
    fun extractHtmlFragmentFromClipboard(clipboardContents: String) = clipboardContents
        .split("<!--StartFragment-->")
        .getOrNull(1)
        ?.split("<!--EndFragment-->")
        ?.first()
}