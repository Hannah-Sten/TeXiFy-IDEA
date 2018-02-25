package nl.rubensten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.rubensten.texifyidea.lang.LatexCommand
import nl.rubensten.texifyidea.lang.Package
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.LatexCommands
import java.io.IOException
import java.io.InputStream

/**
 * @author Sten Wessel
 */
class LatexDocumentationProvider : DocumentationProvider {

    companion object {

        private val PACKAGE_COMMANDS = setOf("\\usepackage", "\\RequirePackage")
    }

    override fun getQuickNavigateInfo(psiElement: PsiElement, originalElement: PsiElement) = when (psiElement) {
        is LatexCommands -> LabelDeclarationLabel(psiElement).makeLabel()
        is BibtexId -> IdDeclarationLabel(psiElement).makeLabel()
        else -> null
    }

    override fun getUrlFor(element: PsiElement, originalElement: PsiElement): List<String>? {
        if (element !is LatexCommands) {
            return null
        }

        // Special case for package inclusion commands
        if (element.name in PACKAGE_COMMANDS) {
            val pkg = element.requiredParameters.getOrNull(0) ?: return null
            return runTexdoc(Package(pkg))
        }

        val command = LatexCommand.lookup(element) ?: return null
        return runTexdoc(command.dependency)
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        originalElement ?: return null
        val urls = getUrlFor(element, originalElement) ?: return null

        if (urls.isEmpty()) {
            return null
        }

        val sb = StringBuilder("<h3>External package documentation</h3>")
        for (url in urls) {
            sb.append("<a href=\"file:///$url\">$url</a><br/>")
        }

        return sb.toString()
    }

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, o: Any?, psiElement: PsiElement?): PsiElement? = null

    override fun getDocumentationElementForLink(psiManager: PsiManager?, s: String?, psiElement: PsiElement?): PsiElement? = null

    private fun runTexdoc(pkg: Package): List<String> {
        val name = if (pkg == Package.DEFAULT) "source2e" else pkg.name

        val stream: InputStream
        try {
            stream = Runtime.getRuntime().exec("texdoc -l $name").inputStream
        } catch (e: IOException) {
            return emptyList()
        }

        val lines = stream.bufferedReader().use { it.readLines() }

        return if (lines.getOrNull(0)?.endsWith("could not be found.") == true) {
             emptyList()
        } else {
            lines
        }
    }
}
