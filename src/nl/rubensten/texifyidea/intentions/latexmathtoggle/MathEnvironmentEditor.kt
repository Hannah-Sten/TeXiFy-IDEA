package nl.rubensten.texifyidea.intentions.latexmathtoggle

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.util.endOffset
import nl.rubensten.texifyidea.util.lineIndentationByOffset
import nl.rubensten.texifyidea.util.runWriteAction
import nl.rubensten.texifyidea.util.trimRange

class MathEnvironmentEditor(private val oldEnvName: String,
                            private val newEnvName: String,
                            val editor: Editor,
                            val environment: PsiElement) {
    /**
     * Apply the conversion of a math environment.
     */
    fun apply() {
        val document = editor.document
        val indent = document.lineIndentationByOffset(environment.textOffset)

        // The number of characters to replace after the end block of the old environment ends.
        val extra = if (newEnvName == "inline") indent.length else if (oldEnvName == "inline") 1 else 0
        // Extra new line to be added at the end of the new environment if the old environment was inline.
        val extraNewLine = if (oldEnvName == "inline") "\n$indent" else ""

        // The number of characters to replace before the begin block of the old environment starts.
        val whitespace = if (newEnvName == "inline") indent.length + 1 else if (oldEnvName == "inline") 1 else 0
        // Extra white space to be added to the beginning of the new environment, when converting from/to inline.
        val extraWhiteSpace = if (newEnvName == "inline") " " else if (oldEnvName == "inline") "\n$indent" else ""

        // Convert the body to one line if necessary.
        val body = if (isOneLineEnvironment(newEnvName)) {
            OneLiner(getBody(indent)).getOneLiner()
        } else {
            getBody(indent)
        }

        val newText = extraWhiteSpace + beginBlock(indent) +
                body.replace("\n", "\n$indent    ") +
                endBlock(indent) + extraNewLine

        runWriteAction {
            document.replaceString(environment.textOffset - whitespace,
                    environment.endOffset() + extra,
                    newText)
        }
    }

    /**
     * Determines if the environment is a one line environment.
     */
    private fun isOneLineEnvironment(envName: String): Boolean {
        return envName == "inline" || envName == "display" || envName == "equation" || envName == "equation*"
    }

    /**
     * Construct the begin block for an environment.
     */
    private fun beginBlock(indent: String): String = when (newEnvName) {
        "inline" -> "$"
        "display" -> "\\[\n$indent    "
        else -> "\\begin{$newEnvName}\n$indent    "
    }

    /**
     * Construct the end block for an environment.
     */
    private fun endBlock(indent: String): String = when (newEnvName) {
        "inline" -> "$"
        "display" -> "\n$indent\\]"
        else -> "\n$indent\\end{$newEnvName}"
    }

    /**
     * Remove the begin and end blocks from an environment, keeping only the body regardless of indentation
     * in the editor.
     */
    private fun getBody(indent: String): String = when (oldEnvName) {
        "inline" -> environment.text.trimRange(1, 1).trim()
        "display" -> environment.text.trimRange(2, 2).replace("$indent    ", "").trim()
        else -> {
            environment.text.trimRange("\\begin{}".length + oldEnvName.length,
                    "\\end{}".length + oldEnvName.length)
                    .replace("$indent    ", "").trim()
        }
    }
}