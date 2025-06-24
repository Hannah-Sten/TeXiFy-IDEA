package nl.hannahsten.texifyidea.documentation

import arrow.core.Either
import arrow.core.raise.either
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.CommandFailure
import nl.hannahsten.texifyidea.lang.Dependend
import nl.hannahsten.texifyidea.lang.Described
import nl.hannahsten.texifyidea.lang.Environment
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.settings.sdk.TexliveSdk
import nl.hannahsten.texifyidea.util.SystemEnvironment
import nl.hannahsten.texifyidea.util.containsAny
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.*
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

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

    /**
     * Check if this is a command which includes a package, in which case we should show docs for the package that is included instead of the command itself.
     */
    private fun isPackageInclusionCommand(element: PsiElement?): Boolean {
        if (element !is LatexCommands) {
            return false
        }

        val command = LatexCommand.lookup(element)
        if (command.isNullOrEmpty()) return false
        return command.first().commandWithSlash in CommandMagic.packageInclusionCommands
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
        return getUrlForElement(element).getOrNull()
    }

    private fun getUrlForElement(element: PsiElement?): Either<CommandFailure, List<String>?> = either {
        if (element !is LatexCommands) {
            return@either null
        }

        val command = LatexCommand.lookup(element)

        if (command.isNullOrEmpty()) return@either null

        // Special case for package inclusion commands
        if (isPackageInclusionCommand(element)) {
            val pkg = element.requiredParametersText().getOrNull(0) ?: return@either null
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

        val stringBuilder = StringBuilder("<h3>${entry.getTitle()} (${entry.getYear()})</h3>")
        stringBuilder.append(entry.getAuthors().joinToString(", ") { a -> formatAuthor(a) })
        stringBuilder.append("<br/><br/>")
        stringBuilder.append(entry.getAbstract())
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
            when(element) {
                is LatexCommands -> {
                    lookup = LatexCommand.lookup(element)?.firstOrNull()
                }
                is LatexEnvIdentifier -> {
                    lookup = element.name?.let { envName ->
                        Environment[envName] ?: Environment.lookupInIndex(envName, element.project).firstOrNull()
                    }
                }
            }
        }
        var docString = if (lookup != null) lookup?.description else ""

        // Link to package docs
        originalElement ?: return null
        val urlsMaybe = if (lookup is Dependend && !isPackageInclusionCommand(element)) runTexdoc((lookup as? Dependend)?.dependency) else getUrlForElement(
            element
        )
        val urlsText = urlsMaybe.fold(
            { it.output },
            { urls -> urls?.joinToString(separator = "<br>") { "<a href=\"file:///$it\">$it</a>" } }
        )

        // Add a line break if necessary
        if (docString?.isNotBlank() == true && urlsText?.isNotBlank() == true) {
            docString += "<br>"
        }

        docString += urlsText

        if (element.previousSiblingIgnoreWhitespace() == null) {
            lookup = null
        }

        // If we return a blank string, the popup will just say "Fetching documentation..."
        return docString.ifBlank { "<br>" }
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

    /**
     * Find list of documentation urls.
     */
    private fun runTexdoc(pkg: LatexPackage?): Either<CommandFailure, List<String>> = either {
        if (pkg == null) return@either emptyList()

        // base/lt... files are documented in source2e.pdf
        val name = if (pkg.fileName.isBlank() || (pkg.name.isBlank() && pkg.fileName.startsWith("lt"))) "source2e" else pkg.fileName

        val command = if (TexliveSdk.Cache.isAvailable) {
            // -M to avoid texdoc asking to choose from the list
            listOf("texdoc", "-l", "-M", name)
        }
        else {
            if (SystemEnvironment.isAvailable("texdoc")) {
                // texdoc on MiKTeX is just a shortcut for mthelp which doesn't need the -M option
                listOf("texdoc", "-l", name)
            }
            else if (SystemEnvironment.isAvailable("mthelp")) {
                // In some cases, texdoc may not be available but mthelp is
                listOf("mthelp", "-l", name)
            } else
                raise(CommandFailure("Could not find mthelp or texdoc", 0))
        }
        val (output, exitCode) = runCommandWithExitCode(*command.toTypedArray(), returnExceptionMessage = true)
        if (exitCode != 0 || output?.isNotBlank() != true) {
            raise(CommandFailure(output ?: "", exitCode))
        }

        // Assume that if there are no path delimiters in the output, the output is some sort of error message (could be in any language)
        val validLines = output.split("\n").filter { it.containsAny(setOf("\\", "/")) }

        if (validLines.isEmpty()) {
            raise(CommandFailure(output, exitCode))
        }

        validLines.toSet().mapNotNull {
            // Do some guesswork about the format
            if (TexliveSdk.Cache.isAvailable) {
                // Line consists of: name version path optional file description
                it.split("\t").getOrNull(2)
            }
            else {
                // mthelp seems to just output the paths itself
                it
            }
        }
    }
}
