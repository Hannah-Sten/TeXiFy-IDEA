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
open class CdotInspection : TexifyInspectionBase() {

    companion object {

        /**
         * Matches all instances of using a dot
         */
        @Language("RegExp")
        val DOT_INSTEAD_OF_CDOT_PATTERN = Pattern.compile("\\s+(\\.)\\s+")!!
    }

    override fun getDisplayName(): String = "Use of . instead of \\cdot"
    override fun getShortName(): String = "Cdot"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Find all en-dash patterns
        val text = file.text
        val matcher = DOT_INSTEAD_OF_CDOT_PATTERN.matcher(text)
        while (matcher.find()) {
            // Only in math mode.
            val element = file.findElementAt(matcher.start()) ?: continue
            if (!element.inMathContext()) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    file,
                    TextRange(matcher.start(), matcher.end()),
                    "\\cdot expected",
                    ProblemHighlightType.WEAK_WARNING,
                    isOntheFly,
                    CdotFix(matcher.start(1))
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private open class CdotFix(val cdotOffset: Int) : LocalQuickFix {

        override fun getFamilyName(): String = "Change to \\cdot"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement as PsiFile
            val document = file.document() ?: return

            document.replaceString(cdotOffset, cdotOffset + 1, "\\cdot")
        }
    }
}