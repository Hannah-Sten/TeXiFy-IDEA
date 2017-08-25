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
open class EnDashInspection : TexifyInspectionBase() {

    companion object {

        /**
         * Finds all the instances of related stuff, i.e. all wrong and good instances of using en dashes.
         *
         * Each match is an en-dash pattern.
         * Group 1 is the first number.
         * Group 2 is the second number.
         *
         * Yes. This is a monstrosity of a regex. I love it. You should love it too. It does magic. And magic is cool.
         * The regex is so cool, that I put it in the docs so you see it when you CTRL+Q this regex.
         * It could be worse though.
         *
         * `(?<![\-])([0-9]+)\s*[\- ]+\s*([0-9]+)(?=[^0-9\-])`
         */
        @Language("RegExp")
        val EN_DASH_PATTERN = Pattern.compile("(?<![0-9\\-])([0-9]+)\\s*[\\- ]+\\s*([0-9]+)(?=[^0-9\\-])")!!

        /**
         * This is the only correct way of using en dashes.
         */
        @Language("RegExp")
        val CORRECT_EN_DASH = Pattern.compile("[0-9]+--[0-9]+")!!
    }

    override fun getDisplayName(): String = "En dash in number ranges"
    override fun getShortName(): String = "SpaceAfterAbbreviation"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        // Find all en-dash patterns
        val text = file.text
        val matcher = EN_DASH_PATTERN.matcher(text)
        while (matcher.find()) {
            val match = matcher.group(0)
            val startNumber = matcher.group(1)
            val endNumber = matcher.group(2)

            // Only regard wrong en-dash patterns.
            if (CORRECT_EN_DASH.matcher(match).matches()) {
                continue
            }

            // Only in normal text mode.
            val element = file.findElementAt(matcher.start()) ?: continue
            if (element.inMathContext()) {
                continue
            }

            descriptors.add(manager.createProblemDescriptor(
                    file,
                    TextRange(matcher.start(), matcher.end()),
                    "En dash expected",
                    ProblemHighlightType.WEAK_WARNING,
                    isOntheFly,
                    NormalSpaceFix(matcher.start()..matcher.end(), startNumber, endNumber)
            ))
        }

        return descriptors
    }

    /**
     * @author Ruben Schellekens
     */
    private open class NormalSpaceFix(val range: IntRange, val start: String, val end: String) : LocalQuickFix {

        override fun getFamilyName(): String = "Convert to en dash"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement as PsiFile
            val document = file.document() ?: return

            document.replaceString(range.start, range.endInclusive, "$start--$end")
        }
    }
}