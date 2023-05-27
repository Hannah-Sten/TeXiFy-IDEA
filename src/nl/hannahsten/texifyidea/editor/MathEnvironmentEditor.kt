package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.application.runUndoTransparentWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.DefaultEnvironment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.parser.endOffset

class MathEnvironmentEditor(
    private val oldEnvironmentName: String,
    private val newEnvironmentName: String,
    val editor: Editor,
    val environment: PsiElement
) {

    /**
     * Apply the conversion of a math environment.
     */
    fun apply() {
        val document = editor.document
        val indent = document.lineIndentationByOffset(environment.textOffset)

        // The number of characters to replace before the begin block of the old environment starts.
        val whitespace = if (newEnvironmentName == "inline") indent.length + 1
        else if (oldEnvironmentName == "inline") 1
        else 0

        // Extra white space to be added to the beginning of the new environment, when converting from/to inline.
        val extraWhiteSpace = if (newEnvironmentName == "inline") {
            // Add indentation if indentation of old environment is bigger than the previous line.
            val indentOfPreviousLine = document.lineIndentation(document.getLineNumber(environment.textOffset) - 1)
            // Get the previous line, so we can check if it ends with a sentence separator. In that case the inline should not move to the previous line.
            val previousLine = document.getText(TextRange(document.getLineStartOffset(document.getLineNumber(environment.textOffset) - 1), environment.textOffset))
            when {
                indentOfPreviousLine.length < indent.length -> "\n$indent"
                PatternMagic.sentenceSeparatorAtLineEnd.matcher(previousLine).find() -> "\n$indent"
                else -> " "
            }
        }
        else if (oldEnvironmentName == "inline") {
            // Add a newline if there is no text on the line before the inline environment.
            val prefixOnLine = document.getText(TextRange(document.getLineStartOffset(document.getLineNumber(environment.textOffset)), environment.textOffset))
            if (prefixOnLine.matches(Regex("^\\s*"))) " " else "\n$indent"
        }
        else ""

        // The number of characters to replace after the end block of the old environment ends.
        val extra = if (newEnvironmentName == "inline") {
            // Only add the indentation if the next line is indented by at least the same amount as the end command
            // of the old environment.
            val nextLineIndent = document.lineIndentation(
                document.getLineNumber(environment.endOffset()) + 1
            )
            if (nextLineIndent.length < indent.length) 0 else indent.length
        }
        else if (oldEnvironmentName == "inline") 1
        else 0

        // Extra new line to be added at the end of the new environment if the old environment was inline.
        val extraNewLine = if (oldEnvironmentName == "inline") {
            // If the rest of the line is empty, add a new line without indentation.
            val restOfLine = document.getText(
                TextRange(
                    environment.endOffset(),
                    document.getLineEndOffset(document.getLineNumber(environment.endOffset()))
                )
            )
            if (restOfLine.matches(Regex("^\\s*"))) "\n" else "\n$indent"
        }
        else ""

        // Convert the body to one line if necessary.
        val originalBody = if (isOneLineEnvironment(newEnvironmentName)) {
            MultiLineMathContentToOneLine(getBody(indent)).getOneLiner()
        }
        else {
            getBody(indent)
        }

        // Deal with the parameter from the alignat environments.
        val body = if (needsParameter(oldEnvironmentName) && !needsParameter(newEnvironmentName)) {
            originalBody.replaceBefore("}", "").removePrefix("}").removePrefix("\n")
        }
        else {
            originalBody
        }

        val newText = extraWhiteSpace + beginBlock(indent) +
            body.replace("\n", "\n$indent    ") +
            endBlock(indent) + extraNewLine

        runUndoTransparentWriteAction {
            document.replaceString(
                environment.textOffset - whitespace,
                environment.endOffset() + extra,
                newText
            )
            // Place caret at the end of math content (works most of the time).
            editor.caretModel.moveToOffset(
                environment.textOffset +
                    newText.length - extra - extraWhiteSpace.length - extraNewLine.length - endBlock(indent).length
            )
            val file = environment.containingFile
                ?: return@runUndoTransparentWriteAction
            if (isAmsMathEnvironment(newEnvironmentName) && LatexPackage.AMSMATH !in file.includedPackages()) {
                file.insertUsepackage(LatexPackage.AMSMATH)
            }
        }
    }

    private fun isAmsMathEnvironment(environmentName: String): Boolean {
        val amsMathEnvironments: Array<String> = DefaultEnvironment.values()
            .filter { it.dependency == LatexPackage.AMSMATH }
            .map { it.environmentName }
            .toTypedArray()
        return amsMathEnvironments.contains(environmentName)
    }

    /**
     * Determines if the environment is a one line environment.
     */
    private fun isOneLineEnvironment(environmentName: String): Boolean {
        return environmentName == "inline" || environmentName == "display" || environmentName == "equation" || environmentName == "equation*"
    }

    private fun needsParameter(environmentName: String): Boolean {
        return environmentName.contains("alignat")
    }

    /**
     * Construct the begin block for an environment.
     * Removes, keeps, or adds the curly braces when the environment needs a parameter.
     */
    private fun beginBlock(indent: String): String = if (needsParameter(newEnvironmentName)) {
        if (needsParameter(oldEnvironmentName)) "\\begin{$newEnvironmentName}"
        else "\\begin{$newEnvironmentName}{}\n$indent    "
    }
    else {
        when (newEnvironmentName) {
            "inline" -> "$"
            "display" -> "\\[\n$indent    "
            else -> "\\begin{$newEnvironmentName}\n$indent    "
        }
    }

    /**
     * Construct the end block for an environment.
     */
    private fun endBlock(indent: String): String = when (newEnvironmentName) {
        "inline" -> "$"
        "display" -> "\n$indent\\]"
        else -> "\n$indent\\end{$newEnvironmentName}"
    }

    /**
     * Remove the begin and end blocks from an environment, keeping only the body regardless of indentation
     * in the editor.
     */
    private fun getBody(indent: String): String = when (oldEnvironmentName) {
        "inline" -> environment.text.trimRange(1, 1).trim()
        "display" -> environment.text.trimRange(2, 2).replace("$indent    ", "").trim()
        else -> {
            environment.text
                .trimRange(
                    "\\begin{}".length + oldEnvironmentName.length,
                    "\\end{}".length + oldEnvironmentName.length
                )
                .replace("$indent    ", "")
                .trim()
        }
    }
}