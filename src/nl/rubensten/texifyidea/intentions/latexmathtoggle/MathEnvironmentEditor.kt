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
    fun apply() {
        val document = editor.document
        val indent = document.lineIndentationByOffset(environment.textOffset)

        val body = if (isOneLineEnvironment(newEnvName)) {
            OneLiner(getBody(indent)).getOneLiner()
        } else {
            getBody(indent)
        }
        println(body)
        val newText = beginBlock(indent) + body.replace("\n", "\n$indent    ") + endBlock(indent)
        println(environment.text)
        println(newText)
        runWriteAction {
            document.replaceString(environment.textOffset, environment.endOffset(), newText)
        }
    }

    private fun isOneLineEnvironment(envName: String): Boolean {
        return envName == "inline" || envName == "display" || envName == "equation"
    }

    private fun beginBlock(indent: String): String = when (newEnvName) {
        "inline" -> "$"
        "display" -> "\\[\n$indent    "
        else -> "\\begin{$newEnvName}\n$indent    "
    }

    private fun endBlock(indent: String): String = when (newEnvName) {
        "inline" -> "$"
        "display" -> "\n$indent\\]"
        else -> "\n$indent\\end{$newEnvName}"
    }

    private fun getBody(indent: String): String = when (oldEnvName) {
        "inline" -> environment.text.trimRange(1, 1).trim()
        "display" -> environment.text.trimRange(2, 2).replace("$indent    ", "").trim()
        else -> {
            environment.text.trimRange("\\begin{}".length + oldEnvName.length, "\\end{}".length + oldEnvName.length)
                    .replace("$indent    ", "").trim()
        }
    }
}