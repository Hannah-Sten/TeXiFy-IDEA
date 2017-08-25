package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.childrenOfType
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.endOffset
import nl.rubensten.texifyidea.util.inMathContext
import org.intellij.lang.annotations.Language
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class SpaceAfterAbbreviationInspection : TexifyInspectionBase() {

    companion object {

        /**
         * Finds all abbreviations that have at least two letters seperated by comma's.
         *
         * It might be more parts, like `b.v.b.d.` is a valid abbreviation. Likewise are `sajdflkj.asdkfj.asdf` and
         * `i.e.`. Single period abbreviations are not being detected as they can easily be confused with two letter words
         * at the end of the sentece (also localisation...) For this there is a quickfix in [LineBreakInspection].
         */
        @Language("RegExp")
        val ABBREVIATION = Pattern.compile("[0-9A-Za-z]+\\.[0-9A-Za-z].")!!
    }

    override fun getDisplayName(): String = "Normal space after abbreviation"
    override fun getShortName(): String = "SpaceAfterAbbreviation"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val texts = file.childrenOfType<LatexNormalText>()
        for (text in texts) {
            if (text.inMathContext()) {
                continue
            }

            val matcher = ABBREVIATION.matcher(text.text)
            while (matcher.find()) {
                if (!isFollowedByWhitespace(text, matcher) || text.text.length < matcher.end()) {
                    continue
                }

                descriptors.add(manager.createProblemDescriptor(
                        text,
                        TextRange(matcher.end() - 1, matcher.end() + 1),
                        "Abbreviation is not followed by a normal space",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        NormalSpaceFix(IntRange(matcher.end(), matcher.end() + 1))
                ))
            }
        }

        return descriptors
    }

    private fun isFollowedByWhitespace(text: LatexNormalText, matcher: Matcher): Boolean {
        // Whitespace followed in the Normal Text.
        val string = text.text
        if (text.text.length > matcher.end()) {
            val spaceMaybe = string.substring(matcher.end(), matcher.end() + 1)
            if (matcher.end() < string.length && spaceMaybe.matches(Regex("\\s+"))) {
                return true
            }
        }

        // Whitespace as PsiWhitespace
        val content = text.parent?.parent ?: return false
        return content.nextSibling is PsiWhiteSpace
    }

    /**
     * @author Ruben Schellekens
     */
    private open class NormalSpaceFix(val whitespaceRange: IntRange) : LocalQuickFix {

        override fun getFamilyName(): String = "Insert normal space"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as LatexNormalText
            val file = element.containingFile
            val document = file.document() ?: return

            // First try psi whitespace.
            val content = element.parent?.parent
            val whitespace = content?.nextSibling as? PsiWhiteSpace
            if (whitespace != null) {
                replaceWhitespace(document, whitespace)
            }
            // Otherwise, replace text.
            else {
                replaceNormalText(document, element)
            }
        }

        private fun replaceWhitespace(document: Document, whiteSpace: PsiWhiteSpace) {
            document.replaceString(whiteSpace.textOffset, whiteSpace.endOffset(), "\\ ")
        }

        private fun replaceNormalText(document: Document, normalText: LatexNormalText) {
            val start = normalText.textOffset + whitespaceRange.start
            val end = normalText.textOffset + whitespaceRange.endInclusive
            document.replaceString(start, end, "\\ ")
        }
    }
}