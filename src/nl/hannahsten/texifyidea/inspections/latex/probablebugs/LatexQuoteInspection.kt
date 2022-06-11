package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTreeUtil.findChildrenOfType
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.inspections.TexifyRegexInspection
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.magic.GeneralMagic
import nl.hannahsten.texifyidea.util.magic.PatternMagic
import nl.hannahsten.texifyidea.util.magic.RegexPattern
import nl.hannahsten.texifyidea.util.requiredParameter
import nl.hannahsten.texifyidea.util.toTextRange
import java.util.regex.Pattern
import javax.annotation.RegEx

/**
 * @author Michael Milton
 */
class LatexQuoteInspection : TexifyInspectionBase() {
    override val inspectionGroup = InsightGroup.LATEX;
    override val inspectionId = "AsciiQuotes";

    /**
     * Matches anything that looks like a LaTeX quote
     */
    private val quotePattern = """["'`]+""".toRegex();

    /**
     * Defines how to extract a quote pair from a command
     */
    data class QuoteMaker(val command: String, val openParam: Int, val closeParam: Int)

    /**
     * Matches any quote expression
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
    );

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        // Build up a list of acceptable quote pairs. The ith index in both arrays must contain a matching quote
        // pair. Additional quote pairs are extracted from the csquotes quote making commands
        val openers = LinkedHashSet(arrayListOf("`", "``"));
        val closers = LinkedHashSet(arrayListOf("'", "''"));
        for (quoteMaker in quoteMakers) {
            for (command in file.commandsInFile(quoteMaker.command)) {
                val opener = command.requiredParameter(quoteMaker.openParam);
                val closer = command.requiredParameter(quoteMaker.closeParam);

                if (!opener.isNullOrEmpty() && !closer.isNullOrEmpty()) {
                    openers.add(opener);
                    closers.add(opener);
                }
            }
        }

        // Process each of the quote tokens individually
        val quoteStack = ArrayDeque<Int>();
        val issues = descriptorList();
        for (text in findChildrenOfType(file, LatexNormalText::class.java)) {
            for (match in quotePattern.findAll(text.text)) {
                val openIndex = openers.indexOf(match.value);
                if (openIndex > -1) {
                    // First case: we find a valid opener
                    // In that case, push the ID of that opener onto the stack
                    quoteStack.add(openIndex);
                }
                else {
                    val closeIndex = closers.indexOf(match.value);
                    if (closeIndex == -1) {
                        // Second case: we find a token that's neither a valid opener nor closer
                        issues.add(
                            manager.createProblemDescriptor(
                                text,
                                match.range.toTextRange(),
                                "${match.value} is not a valid set of LaTex quotes",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOntheFly
                            )
                        )
                    }
                    else if (!quoteStack.isEmpty() && closeIndex == quoteStack.last()) {
                        // Third case: we find a closer that's correctly paired.
                        // In this case, we can pop the stack.
                        quoteStack.removeLast();
                    }
                    else {
                        // Fourth case: we find a closer that's incorrectly paired
                        issues.add(
                            manager.createProblemDescriptor(
                                text,
                                match.range.toTextRange(),
                                "Closing quote without opening quote",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                isOntheFly
                            )
                        )
                    }
                }
            }
        }
        return issues;
    }
}
