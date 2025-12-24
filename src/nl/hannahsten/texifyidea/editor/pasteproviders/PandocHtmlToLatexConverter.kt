package nl.hannahsten.texifyidea.editor.pasteproviders

import nl.hannahsten.texifyidea.util.runCommandWithExitCode

/**
 * Use Pandoc to convert HTML to LaTeX.
 *
 * This is not a HtmlToLatexConverter because it operates on the whole html at once, not on individual nodes.
 */
class PandocHtmlToLatexConverter {

    companion object {

        val isPandocInPath: Boolean by lazy {
            "pandoc -v".runCommandWithExitCode().second == 0
        }
    }

    /**
     * Take the given html and call Pandoc to convert it to LaTeX.
     */
    fun translateHtml(htmlIn: String): String? {
        if (!isPandocInPath) return null

        return if (isPandocInPath) {
            val commands = arrayOf(
                "pandoc",
                "-f",
                "html",
                "-t",
                "latex"
            )
            val (output, exitCode) = runCommandWithExitCode(*commands, inputString = htmlIn)
            if (exitCode != 0 || output == null) return null
            output
        }
        else null
    }
}