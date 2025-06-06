package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.grammar.strategy.StrategyUtils
import com.intellij.grazie.text.TextContent
import com.intellij.grazie.text.TextExtractor
import com.intellij.lang.tree.util.children
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.index.LatexDefinitionIndex
import nl.hannahsten.texifyidea.lang.commands.Argument
import nl.hannahsten.texifyidea.lang.commands.LatexCommand
import nl.hannahsten.texifyidea.lang.commands.LatexRegularCommand
import nl.hannahsten.texifyidea.lang.commands.RequiredArgument
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.parser.*

/**
 * Explains to Grazie which psi elements contain text and which don't.
 */
class LatexTextExtractor : TextExtractor() {

    override fun buildTextContent(root: PsiElement, allowedDomains: MutableSet<TextContent.TextDomain>): TextContent? {
        if (root !is LatexContent) {
            // It is important to return null instead of TextContent, otherwise Grazie will not search higher up in the psi tree, and we will not get this function called for non-leaf elements and the above lines will not work
            return null
        }

        return buildTextContent(root)
    }

    fun buildTextContent(root: LatexContent): TextContent? {
        // TODO: Performance
        // Since Grazie works by first checking leaf elements, and if it gets null tries one level higher, we cannot return anything (e.g. literal for a command, comment for comments) other than LatexContent because then LatexContent itself will not be used as a root.
        // However, we do need it as a root because we need to filter out certain things like inline math ourselves, so that we can make sure all the whitespace around ignored items is correct.
        val domain = TextContent.TextDomain.PLAIN_TEXT

        // #4044
        if (root.textLength > root.containingFile.textLength) {
            return null
        }

        val textContent = TextContent.builder().build(root, domain) ?: return null
        val stealthyRanges = getStealthyRanges(root)
            // Convert IntRange (inclusive end) to TextRange (exclusive end)
            .map { TextContent.Exclusion.exclude(it.toTextRange()) }
            .filter { it.start >= 0 && it.end <= textContent.length }

        val textToSubmit = textContent.excludeRanges(stealthyRanges)
        return textToSubmit
    }

    /**
     * Get ranges to ignore.
     * Note: IntRange has an inclusive end.
     */
    fun getStealthyRanges(root: PsiElement): List<IntRange> {
        // TODO: performance
        // Getting text takes time, so we only do it once
        val rootText = root.text

        // Only keep normaltext, assuming other things (like inline math) need to be ignored.
        val ranges = (root.childrenOfType(LatexNormalText::class) + root.childrenOfType<LatexParameterText>() + root.childrenOfType<PsiWhiteSpace>() + root.childrenOfType<LatexCommands>())
            .asSequence()
            .filter { !it.inMathContext() && it.isNotInSquareBrackets() }
            // Ordering is relevant for whitespace
            .sortedBy { it.startOffset }
            // Always keep newlines, as they may be the only whitespace splitting consecutive commands
            .filter { text -> text !is PsiWhiteSpace || text.text.contains("\n") }
            // Skip arguments of non-text commands, but keep arguments of unknown commands, in particular if they are in the middle of a sentence
            // Even commands which have no text as argument, for example certain reference commands like autoref, may need to be kept in to get correct punctuation
            .filter { text ->
                if (text is LatexCommands) {
                    isUserDefinedTextCommand(text.name ?: return@filter false, text.project)
                }
                else {
                    !hasNonTextArgument(text.firstParentOfType(LatexCommands::class)?.name ?: return@filter true, text.project)
                }
            }
            // Environment names are never part of a sentence
            .filterNot { text -> text.firstParentOfType<LatexBeginCommand>() != null || text.firstParentOfType<LatexEndCommand>() != null }
            // If we encounter an unescaped &, we are in some language construct like a tabular, so we ignore this because often a tabular does not contain full sentences
            .filter { text -> text.node.children().none { it.elementType == LatexTypes.AMPERSAND } }
            // NOTE: it is not allowed to start the text we send to Grazie with a newline! If we do, then Grazie will just not do anything. So we exclude whitespace at the start
            .dropWhile { it is PsiWhiteSpace }
            // Ranges that we need to keep
            // Note that textRangeInParent will not be correct because that's the text range in the direct parent, not in the root
            .flatMap { text ->
                var start = text.textRange.startOffset - root.startOffset
                // If LatexNormalText starts after a newline following a command, the newline is not part of the LatexNormalText so we include it manually to make sure that it is seen as a space between sentences
                // NOTE: it is not allowed to start the text we send to Grazie with a newline! If we do, then Grazie will just not do anything. So we exclude the newline for the first normal text in the file.
                if (setOf(' ', '\n').contains(rootText.getOrNull(start - 1)) && root.childrenOfType(LatexNormalText::class).firstOrNull() != text
                ) {
                    //  We have to skip over indents to find the newline though (indents will be ignored later)
                    start -= rootText.substring(0, start).takeLastWhile { it.isWhitespace() }.length
                }

                // -1 Because endOffset is exclusive, but we are working with inclusive end here
                var end = text.textRange.endOffset - 1 - root.startOffset
                // If LatexNormalText ends, for example because it is followed by a command, we do want to include the space in front of the command, since it is still typeset as a space, which is not true for the space after the command if the command has no arguments,
                // except when the space is followed by inline math, since we ignore inline math altogether (which is probably not correct) we should also ignore the space
                if (setOf(' ', '\n').contains(rootText.getOrNull(end + 1)) && rootText.getOrNull(end + 2) != '$' && rootText.substringOrNull(end + 2, end + 4) != "\\(") {
                    end += 1
                }
                listOf(start, end)
            }
            .sorted()
            .toMutableList()
            // Make sure that if the root does not start/end with normal text, that those parts are excluded
            .also { it.add(0, -1) }
            .also { it.add(root.endOffset()) }
            // To get the ranges that we need to ignore
            // + 1 because we want to exclude all letters _after_ the last letter that we want to keep (the ranges defined above are inclusive)
            // -1 because IntRange has inclusive end, but we want to exclude all letters _excluding_ the letter where the normal text started
            .chunked(2) { IntRange(it[0] + 1, it[1] - 1) }
            .filter { it.first <= it.last && it.first >= 0 && it.last < rootText.length }
            .toMutableSet()

        // There is still a bit of a problem, because when stitching together the NormalTexts, whitespace is lost
        // so this leads Grazie to think that there is no space there, while in fact there may or may not be

        // Add indents as ranges to ignore
        // Currently, GrammarChecker does not handle overlapped ranges, so we do that ourselves
        for (indent in StrategyUtils.indentIndexes(root.text, setOf(' '))) {
            val overlapped = ranges.filter { indent.overlaps(it) }
            ranges.removeAll(overlapped.toSet())
            ranges.add(indent.merge(overlapped))
        }
        return ranges.sortedBy { it.first }
    }

    /**
     * Keep the command if the command will probably be replaced by some text in the typeset document, e.g. \texttt{arg} should read just "arg" to Grazie
     */
    private fun hasNonTextArgument(commandName: String, project: Project): Boolean {
        return LatexCommand.lookup(commandName)
            ?.firstOrNull()
            ?.arguments
            ?.filter { it is RequiredArgument }
            // Do not keep if it is not text
            ?.any { it.type != Argument.Type.TEXT && it.type != Argument.Type.LABEL } == true
    }

    /**
     * Maybe it is a user defined command which contains text.
     * Special case: if the command does not have parameters but the definition contains a text command, we assume the command itself will fit into the sentence (as we can't do a text replacement before sending to Grazie).
     */
    private fun isUserDefinedTextCommand(commandName: String, project: Project): Boolean {
        // todo cache
        val allTextCommands = LatexRegularCommand.ALL.filter { command -> command.arguments.any { it.type != Argument.Type.TEXT && it.type != Argument.Type.LABEL } }.map { it.commandWithSlash }.toSet()
        return LatexDefinitionIndex.Util.getItems(project).filter { it.definedCommandName() == commandName }.any { it.text.containsAny(allTextCommands) }
    }

    private fun PsiElement.isNotInSquareBrackets() = parents().find { it is LatexGroup || it is LatexOptionalParam }
        ?.let { it is LatexGroup } ?: true
}