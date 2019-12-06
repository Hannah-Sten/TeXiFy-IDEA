package nl.hannahsten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexCommand
import nl.hannahsten.texifyidea.lang.Package
import nl.hannahsten.texifyidea.lang.Package.Companion.DEFAULT
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.LatexDistribution
import nl.hannahsten.texifyidea.util.previousSiblingIgnoreWhitespace
import java.io.IOException
import java.io.InputStream

/**
 * @author Sten Wessel
 */
class LatexDocumentationProvider : DocumentationProvider {

    companion object {

        private val PACKAGE_COMMANDS = setOf("usepackage", "RequirePackage", "documentclass", "LoadClass")
    }

    /**
     * The currently active lookup item.
     */
    private var lookup: Described? = null

    override fun getQuickNavigateInfo(psiElement: PsiElement, originalElement: PsiElement) = when (psiElement) {
        is LatexCommands -> LabelDeclarationLabel(psiElement).makeLabel()
        is BibtexId -> IdDeclarationLabel(psiElement).makeLabel()
        else -> null
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
        if (element !is LatexCommands) {
            return null
        }

        val command = LatexCommand.lookup(element) ?: return null

        // Special case for package inclusion commands
        if (command.command in PACKAGE_COMMANDS) {
            val pkg = element.requiredParameters.getOrNull(0) ?: return null
            return runTexdoc(Package(pkg))
        }

        return runTexdoc(command.dependency)
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        if (element.previousSiblingIgnoreWhitespace() != null) {
            return lookup?.description
        }
        else lookup = null

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

    override fun getDocumentationElementForLookupItem(psiManager: PsiManager?, obj: Any?, psiElement: PsiElement?): PsiElement? {
        if (obj == null || obj !is Described) {
            lookup = null
            return null
        }

        lookup = obj
        return psiElement
    }

    override fun getDocumentationElementForLink(psiManager: PsiManager?, s: String?, psiElement: PsiElement?): PsiElement? = null

    private fun runTexdoc(pkg: Package): List<String> {
        val name = if (pkg == DEFAULT) "source2e" else pkg.name

        val stream: InputStream
        try {
            // -M to avoid texdoc asking to choose from the list
            val command = if (LatexDistribution.isTexlive) {
                "texdoc -l -M $name"
            }
            else {
                // texdoc on MiKTeX is just a shortcut for mthelp which doesn't need the -M option
                "texdoc -l $name"
            }
            stream = Runtime.getRuntime().exec(command).inputStream
        }
        catch (e: IOException) {
            return emptyList()
        }

        val lines = stream.bufferedReader().use { it.readLines() }

        return if (lines.getOrNull(0)?.endsWith("could not be found.") == true) {
             emptyList()
        }
        else {
            if (LatexDistribution.isTexlive) {
                lines.map {
                    // Line consists of: name version path optional file description
                    it.split("\t")[2]
                }
            }
            else {
                lines
            }
        }
    }
}
