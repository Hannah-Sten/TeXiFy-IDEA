package nl.hannahsten.texifyidea.documentation

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parentOfType
import nl.hannahsten.texifyidea.util.parentsOfType
import nl.hannahsten.texifyidea.util.previousSiblingIgnoreWhitespace
import java.io.IOException
import java.io.InputStream

/**
 * @author Sten Wessel
 */
class LatexDocumentationProvider : DocumentationProvider {

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
        if (command.first().commandWithSlash in CommandMagic.packageInclusionCommands) {
            val pkg = element.requiredParameters.getOrNull(0) ?: return null
            return runTexdoc(LatexPackage(pkg))
        }

        return runTexdoc(command.first().dependency)
    }

    /**
     * Generate the content that should be shown in the documentation popup.
     */
    private fun generateDocForBibtexId(element: BibtexId): String? {
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

    /**
     * Generate the content that should be shown in the documentation popup.
     * Works for commands and environments
     */
    private fun generateDocForLatexCommandsAndEnvironments(element: PsiElement, originalElement: PsiElement?): String? {
        // Indexed documentation
        if (lookup == null) {
            // Apparently the lookup item is not yet initialised, so let's do that first
            // Can happen when requesting documentation for an item for which the user didn't request documentation during autocompletion
            if (element is LatexCommands) {
                lookup = LatexCommand.lookup(element)?.firstOrNull()
            }
            // If the cursor is on the parameter text inside a begin/end command, we show docs for the environment
            else if (element is LatexParameterText && (element.parentOfType(LatexBeginCommand::class) != null || element.parentOfType(LatexEndCommand::class) != null)) {
                val envName = element.text
                lookup = Environment[envName] ?: Environment.lookupInIndex(envName, element.project).firstOrNull()
            }
        }
        var docString = if (lookup != null) lookup?.description else ""

        // Link to package docs
        originalElement ?: return null
        val urls = if (lookup is Dependend) runTexdoc((lookup as Dependend).dependency) else getUrlFor(element, originalElement)

        if (docString?.isNotBlank() == true && !urls.isNullOrEmpty()) {
            docString += "<br/>"
        }

        if (urls != null) {
            for (url in urls) {
                // Propagate the warning
                docString += if (url.contains("install the texdoc package")) {
                    url
                }
                else {
                    "<a href=\"file:///$url\">$url</a><br/>"
                }
            }
        }

        if (element.previousSiblingIgnoreWhitespace() == null) {
            lookup = null
        }

        // If we return a blank string, the popup will just say "Fetching documentation..."
        return if (docString.isNullOrBlank()) "<br>" else docString
    }

    // originalElement: element under the mouse cursor
    // element: element to which originalElement resolves
    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        // We resolve to the bibtex id (not the entry), see LatexLabelParameterReference
        if (element is BibtexId) {
            return generateDocForBibtexId(element)
        }
        return generateDocForLatexCommandsAndEnvironments(element, originalElement)
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
                if (SystemEnvironment.isAvailable("texdoc")) {
                    // texdoc on MiKTeX is just a shortcut for mthelp which doesn't need the -M option
                    "texdoc -l $name"
                }
                else {
                    // In some cases, texdoc may not be available but mthelp is
                    "mthelp -l $name"
                }
            }
            stream = Runtime.getRuntime().exec(command).inputStream
        }
        catch (e: IOException) {
            return if (e.message?.contains("Cannot run program \"texdoc\"") == true) {
                listOf("<br><i>Tip: install the texdoc package to get links to package documentation here</i>")
            }
            else {
                emptyList()
            }
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
