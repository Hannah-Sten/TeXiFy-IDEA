package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.util.document
import nl.rubensten.texifyidea.util.inMathContext
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class SentenceEndWithCapitalInspection : TexifyInspectionBase() {

    companion object {

        /**
         * Matches all places where sentences end with capital letters.
         */
        @Language("RegExp")
        val SENTENCE_END_WITH_CAPITAL = Pattern.compile("[A-Z\u00C0-\u00DD]\\.[ \\t]*\\n")!!
    }

    override fun getDisplayName(): String = "End-of-sentence space after sentences ending with capitals"
    override fun getShortName(): String = "SentenceEndWithCapital"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Find all en-dash patterns
        val text = file.text
        val matcher = SENTENCE_END_WITH_CAPITAL.matcher(text)
        while (matcher.find()) {
            // Only in normal text mode.
            val element = file.findElementAt(matcher.start()) ?: continue
            if (element.inMathContext()) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    file,
                    TextRange(matcher.start(), matcher.end()),
                    "Sentences ending with a capital should end with an end-of-sentence space",
                    ProblemHighlightType.WEAK_WARNING,
                    isOntheFly,
                    EndOfSentenceSpaceFix(matcher.end() - 2)
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private open class EndOfSentenceSpaceFix(val offset: Int) : LocalQuickFix {

        override fun getFamilyName(): String = "Add an end-of-sentence space"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement as PsiFile
            val document = file.document() ?: return
            document.insertString(offset, "\\@")
        }
    }
}