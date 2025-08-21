package nl.hannahsten.texifyidea.documentation

import arrow.core.Either
import arrow.core.raise.either
import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import nl.hannahsten.texifyidea.CommandFailure
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.lang.LSemanticEntity
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.toLatexPackage
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvIdentifier
import nl.hannahsten.texifyidea.psi.nameWithSlash
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
    private var lookup: LSemanticEntity? = null

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
        return element.nameWithSlash in CommandMagic.packageInclusionCommands
    }

    override fun getUrlFor(element: PsiElement?, originalElement: PsiElement?): List<String>? {
        val bundle = originalElement?.containingFile?.let {
            LatexDefinitionService.getInstance(it.project).getDefBundlesMerged(it)
        }
        return getUrlForElement(element, bundle).getOrNull()
    }

    private fun getUrlForElement(element: PsiElement?, bundle: DefinitionBundle?): Either<CommandFailure, List<String>?> = either {
        if (element !is LatexCommands) {
            return@either null
        }

        val command = bundle?.lookupCommandPsi(element) ?: return@either null
        // Special case for package inclusion commands
        if (command.nameWithSlash in CommandMagic.packageInclusionCommands) {
            val pkg = element.requiredParametersText().getOrNull(0) ?: return@either null
            return runTexdoc(LatexPackage(pkg))
        }

        return runTexdoc(command.dependency.toLatexPackage())
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
     * Regexes and replacements which clean up the documentation.
     *
     * **test**
     * Arguments \[mop\]arg should be left in, because they are needed when adding to the autocomplete
     */
    private val dtxFormattingReplacers = listOf(
        // Commands to remove entirely,, making sure to capture in the argument nested braces
        Pair("""\\(cite|footnote)\{(\{[^}]*}|[^}])+?}\s*""".toRegex()) { "" },
        // \cs command from the doctools package
        Pair("""(?<pre>[^|]|^)\\c[sn]\{(?<command>[^}]+?)}""".toRegex()) { result -> result.groups["pre"]?.value + "<tt>\\" + result.groups["command"]?.value + "</tt>" },
        // Other commands, except when in short verbatim
        Pair("""(?<pre>[^|]|^)\\(?:textsf|textsc|cmd|pkg|env)\{(?<argument>(\{[^}]*}|[^}])+?)}""".toRegex()) { result -> result.groups["pre"]?.value + "<tt>" + result.groups["argument"]?.value + "</tt>" },
        // Replace \textbf with <b> tags
        Pair("""\\textbf\{(?<argument>(\{[^}]*}|[^}])+?)}""".toRegex()) { result -> "<b>${result.groups["argument"]?.value}</b>" },
        // Replace \emph and \textit with <i> tags
        Pair<Regex, (MatchResult) -> String>("""\\(textit|emph)\{(?<argument>(\{[^}]*}|[^}])+?)}""".toRegex()) { result -> "<i>${result.groups["argument"]?.value}</i>" },
        // Short verbatim, provided by ltxdoc
        Pair("""\|""".toRegex()) { "" },
        // While it is true that text reflows in the documentation popup, so we don't need linebreaks, often package authors include an environment or something else
        // which does depend on linebreaks to be readable, and therefore we keep linebreaks by default.
        Pair("""\n""".toRegex()) { "<br>" },
    )

    /**
     * Should format to valid HTML as used in the docs popup.
     * Only done when indexing, but it should still be fast because it can be done up to 28714 times for full TeX Live.
     */
    fun formatDtxSource(docs: String): String {
        var formatted = docs.trim()
        dtxFormattingReplacers.forEach { formatted = it.first.replace(formatted, it.second).trim() }
        return formatted.trim()
    }

    /**
     * Generate the content that should be shown in the documentation popup.
     * Works for commands and environments
     */
    private fun generateDocForLatexCommandsAndEnvironments(element: PsiElement, originalElement: PsiElement?): String? {
        // Indexed documentation
        // Apparently the lookup item is not yet initialised, so let's do that first
        // Can happen when requesting documentation for an item for which the user didn't request documentation during autocompletion
        // In that case we shouldn't reassign lookup because then we would show documentation for the wrong item next time
        val file = element.containingFile
        val defBundle = file?.let { LatexDefinitionService.getInstance(it.project).getDefBundlesMerged(it) }
        val lookupItem = lookup ?: when (element) {
            is LatexCommands -> {
                defBundle?.lookupCommandPsi(element)
            }

            is LatexEnvIdentifier -> {
                defBundle?.lookupEnv(element.name)
            }

            else -> null
        }
        return buildString {
            lookupItem?.description?.let { append(formatDtxSource(it)) }
            // Link to package docs
            originalElement ?: return@buildString
            val urlsMaybe = if (!isPackageInclusionCommand(element)) {
                runTexdoc(lookupItem?.dependency?.toLatexPackage())
            }
            else getUrlForElement(element, defBundle)
            val urlsText = urlsMaybe.fold(
                { it.output },
                { urls -> urls?.joinToString(separator = "<br>") { "<a href=\"file:///$it\">$it</a>" } }
            )

            // Add a line break if necessary
            if (isNotBlank() && urlsText?.isNotBlank() == true) {
                append("<br>")
            }

            append(urlsText)
            if (element.previousSiblingIgnoreWhitespace() == null) {
                lookup = null
            }
            // If we return a blank string, the popup will just say "Fetching documentation..."
            if (isBlank()) {
                append("<br>")
            }
        }
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
        if (obj == null || obj !is LSemanticEntity) {
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
            }
            else
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
