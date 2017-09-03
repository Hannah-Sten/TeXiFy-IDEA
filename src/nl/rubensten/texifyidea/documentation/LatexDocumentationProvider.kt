package nl.rubensten.texifyidea.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.lang.LatexCommand
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.psi.LatexCommands

/**
 *
 * @author Sten Wessel
 */
open class LatexDocumentationProvider : AbstractDocumentationProvider() {

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element is LatexCommands) {
            if ("\\label" == element.name) {
                val label = element.requiredParameters[0]
                val file = element.containingFile.name
                val line = 1 + StringUtil.offsetToLineNumber(element.containingFile.text, element.textOffset)  // Because line numbers do start at 1

                return "Go to declaration of label '$label' [$file:$line]"
            }
        }

        return null
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
        if (element !is LatexCommands) {
            return null
        }

        val command: LatexCommand = LatexCommand.lookup(element) ?: return null

        return runTexdoc(command.getDependency())
    }

    private fun runTexdoc(pkg: Package): List<String> {
        val name = if (pkg == Package.DEFAULT) "source2e" else pkg.name
        val stream = Runtime.getRuntime().exec("texdoc -l ${name}").inputStream

        val lines = stream.bufferedReader().use { it.readLines() }

        return if (lines.getOrNull(0)?.endsWith("could not be found.") == true) {
             emptyList()
        } else {
            lines
        }
    }
}
