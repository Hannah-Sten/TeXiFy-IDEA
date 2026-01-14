package nl.hannahsten.texifyidea.inspections.latex.typesetting

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType
import com.intellij.refactoring.suggested.extend
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LSemanticCommand
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.predefined.AllPredefined
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.traverseCommands
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.PatternMagic.quotePattern
import nl.hannahsten.texifyidea.util.parser.inMathContext
import nl.hannahsten.texifyidea.util.replaceString
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * @author Michael Milton
 */
class LatexQuoteInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX
    override val inspectionId = "Quote"

    /**
     * Defines how to extract a quote pair from a command.
     * @param command The LaTeX command to extract a new quote pair from
     * @param openParam The index of the argument to that command where the opening quote is defined
     * @param closeParam The index of the argument to that command where the closing quote is defined
     */
    data class QuoteMaker(val command: String, val openParam: Int, val closeParam: Int)

    /**
     * All the ways that csquotes defines a new quotation mark
     */
    private val quoteMakers = arrayOf(
        QuoteMaker("\\MakeOuterQuote", 0, 0),
        QuoteMaker("\\MakeInnerQuote", 0, 0),
        QuoteMaker("\\MakeAutoQuote", 0, 1),
        QuoteMaker("\\MakeForeignQuote", 1, 2),
        QuoteMaker("\\MakeBlockQuote", 0, 2),
        QuoteMaker("\\MakeForeignBlockQuote", 1, 3),
        QuoteMaker("\\MakeHyphenBlockQuote", 1, 3),
        QuoteMaker("\\MakeHybridBlockQuote", 1, 3),
    ).associateBy { it.command }

    private val fixers = arrayOf(
        MathFix(),
        LatexQuoteFix("opening single quote", "`"),
        LatexQuoteFix("opening double quote", "``"),
        LatexQuoteFix("closing single quote", "'"),
        LatexQuoteFix("closing double quote", "''"),
    )

    private val fontspecCommandNames = AllPredefined.findByLib(LatexLib.FONTSPEC).mapNotNullTo(mutableSetOf()) {
        if(it is LSemanticCommand) it.nameWithSlash else null
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        // Build up a list of acceptable quote pairs. The ith index in both arrays must contain a matching quote
        // pair. Additional quote pairs are extracted from the csquotes quote making commands
        val validQuotes = arrayListOf(
            "`" to "'",
            "``" to "''",
        )
        val commands = file.traverseCommands()

        // When Ligatures=TeXOff is set, straight quotes will be used. This setting can be applied in certain fontspec commands
        if (commands.filter { it.name in fontspecCommandNames }.any { it.text.contains("ligatures=TeXOff", ignoreCase = true) }) {
            return emptyList()
        }
        for (command in commands) {
            val quoteMaker = quoteMakers[command.name] ?: continue

            val opener = command.requiredParameterText(quoteMaker.openParam)
            val closer = command.requiredParameterText(quoteMaker.closeParam)

            if (!opener.isNullOrEmpty() && !closer.isNullOrEmpty()) {
                validQuotes.add(opener to closer)
            }
        }
        val openers = LinkedHashSet(validQuotes.map { it.first })
        val closers = LinkedHashSet(validQuotes.map { it.second })

        // Process each of the quote tokens individually

        // Each index corresponds to a quote pair in validQuotes
        val unclosedQuoteIndexStack = ArrayDeque<Int>()
        val issues = descriptorList()
        for (text in findChildrenOfType(file, LatexNormalText::class.java)) {
            if (text.inMathContext()) {
                continue
            }
            for (match in quotePattern.findAll(text.text)) {
                val openQuoteIndex = openers.indexOf(match.value)
                if (openQuoteIndex > -1) {
                    // First case: we find a valid opener
                    // In that case, push the ID of that opener onto the stack
                    unclosedQuoteIndexStack.add(openQuoteIndex)
                }
                else {
                    val closeQuoteIndex = closers.indexOf(match.value)
                    if (closeQuoteIndex == -1) {
                        // Second case: we find a token that's neither a valid opener nor closer
                        issues.add(
                            manager.createProblemDescriptor(
                                text,
                                match.range.toTextRange(),
                                "${match.value} is not a valid set of LaTeX quotes",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOntheFly,
                                *fixers
                            )
                        )
                    }
                    else if (!unclosedQuoteIndexStack.isEmpty() && closeQuoteIndex == unclosedQuoteIndexStack.last()) {
                        // Third case: we find a closer that's correctly paired.
                        // In this case, we can pop the stack.
                        unclosedQuoteIndexStack.removeLast()
                    }
                    else if (match.value == "'" && match.range.first > 0 && !text.text[match.range.first - 1].isWhitespace()) {
                        // Fourth case: it looks like an unpaired quote, but it's impossible to tell because it would
                        // also be a valid apostrophe (e.g. it's preceded by non-whitespace), so we ignore it.
                        continue
                    }
                    else {
                        // Fifth case: we find a closer that's incorrectly paired
                        // We never raise this for single ' characters because these are valid apostrophes
                        issues.add(
                            manager.createProblemDescriptor(
                                text,
                                match.range.toTextRange(),
                                "Closing quote without opening quote",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOntheFly,
                                *fixers
                            )
                        )
                    }
                }
            }
        }
        return issues
    }

    /**
     * Fixes an invalid quote by putting it into an inline math environment
     */
    private class MathFix : LocalQuickFix {

        override fun getFamilyName(): String = "Convert to inline maths environment, for typesetting feet, inches or other mathematical punctuation."

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val document = descriptor.psiElement.containingFile.document() ?: return
            val expandedRange = descriptor.textRangeInElement.extend(descriptor.psiElement.text) {
                it.isDigit() || it in """`"'"""
            }
            val originalText = expandedRange.substring(descriptor.psiElement.text)
            document.replaceString(expandedRange.shiftRight(descriptor.psiElement.textOffset), "\\(${originalText}\\)")
        }
    }

    private class LatexQuoteFix(val description: String, val replacement: String) : LocalQuickFix {

        override fun getFamilyName(): String = "Replace with a LaTeX $description"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val document = descriptor.psiElement.containingFile.document() ?: return
            document.replaceString(
                // The text range in the document is the text range in the containing element, shifted forward by
                // the offset of the containing element in the document
                descriptor.textRangeInElement.shiftRight(descriptor.psiElement.textOffset),
                replacement
            )
        }
    }
}
