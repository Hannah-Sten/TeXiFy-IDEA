package nl.hannahsten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.parentsOfType
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
        is BibtexEntry -> IdDeclarationLabel(psiElement).makeLabel()
        else -> null
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
        if (element !is LatexCommands) {
            return null
        }

        val command = LatexCommand.lookup(element)

        if (command.isNullOrEmpty()) return null

        // Special case for package inclusion commands
        if (command.first().command in PACKAGE_COMMANDS) {
            val pkg = element.requiredParameters.getOrNull(0) ?: return null
            return runTexdoc(LatexPackage(pkg))
        }

        return runTexdoc(command.first().dependency)
    }

    // originalElement: element under the mouse cursor
    // element: element to which originalElement resolves
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        // We resolve to the bibtex id (not the entry), see LatexLabelParameterReference
        if (element is BibtexId) {
            val entry = element.parentsOfType(BibtexEntry::class).firstOrNull() ?: return null
            fun formatAuthor(author: String): String {
                val parts = author.split(",")
                if (parts.size < 2) return author
                val last = parts[1].trim()
                val first = parts[0].trim()
                return "$first $last"
            }

            val stringBuilder = StringBuilder("<h3>${entry.title} (${entry.year})</h3>")
            stringBuilder.append(entry.authors.joinToString(", ") { a -> formatAuthor(a) })
            stringBuilder.append("<br/><br/>")
            stringBuilder.append(entry.abstract)
            return stringBuilder.toString()
        }

        // Indexed documentation
        if (lookup == null) {
            // Apparently the lookup item is not yet initialised, so let's do that first
            // Can happen when requesting documentation for an item for which the user didn't request documentation during autocompletion?
            if (element !is LatexCommands) return null
            lookup = LatexCommand.lookup(element)?.firstOrNull()
        }
        var docString = if (lookup != null) lookup?.description else ""

        // Link to package docs
        originalElement ?: return null
        val urls = if (lookup is LatexCommand) runTexdoc((lookup as LatexCommand).dependency) else getUrlFor(element, originalElement)

        if (docString?.isNotBlank() == true && !urls.isNullOrEmpty()) {
            docString += "<br/>"
        }

        if (urls != null) {
            for (url in urls) {
                docString += "<a href=\"file:///$url\">$url</a><br/>"
            }
        }

        if (element.previousSiblingIgnoreWhitespace() == null) {
            lookup = null
        }

        // If we return a blank string, the popup will just say "Fetching documentation..."
        return if (docString.isNullOrBlank()) "<br>" else docString
    }

    override fun getDocumentationElementForLookupItem(
        psiManager: PsiManager?,
        obj: Any?,
        psiElement: PsiElement?
    ): PsiElement? {
        if (obj == null || obj !is Described) {
            // Cancel documentation popup
            lookup = null
            return null
        }

        lookup = obj
        return psiElement
    }

    override fun getDocumentationElementForLink(
        psiManager: PsiManager?,
        s: String?,
        psiElement: PsiElement?
    ): PsiElement? = null

    private fun runTexdoc(pkg: LatexPackage): List<String> {
        // base/lt... files are documented in source2e.pdf
        val name = if (pkg.fileName.isBlank() || (pkg.name.isBlank() && pkg.fileName.startsWith("lt"))) "source2e" else pkg.fileName

        val stream: InputStream
        try {
            // -M to avoid texdoc asking to choose from the list
            val command = if (TexliveSdk.isAvailable) {
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
            if (TexliveSdk.isAvailable) {
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
