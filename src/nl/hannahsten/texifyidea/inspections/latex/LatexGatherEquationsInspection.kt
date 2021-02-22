package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.AMSMATH
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document

/**
 * @author Hannah Schellekens
 */
open class LatexGatherEquationsInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override fun getDisplayName() = "Gather equations"

    override val inspectionId = "GatherEquations"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        file.childrenOfType(LatexNoMathContent::class).asSequence()
            .filter { it.isDisplayMath() }
            .map { Pair(it, it.nextSiblingIgnoreWhitespace()) }
            .filter { (_, next) -> next != null && next is LatexNoMathContent && next.isDisplayMath() }
            .flatMap { sequenceOf(it.first, it.second) }
            .distinct()
            .forEach {
                descriptors.add(
                    manager.createProblemDescriptor(
                        it ?: return@forEach,
                        "Equations can be gathered",
                        GatherEnvironments(),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly
                    )
                )
            }

        return descriptors
    }

    /**
     * @author Hannah Schellekens
     */
    private class GatherEnvironments : LocalQuickFix {

        override fun getFamilyName() = "Gather equations"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as? LatexNoMathContent ?: return
            val file = element.containingFile
            val document = file.document() ?: return

            val equations = findEquations(element)
            val startOffset = equations.first().textOffset
            val endOffset = equations.last().endOffset()
            val indent = document.lineIndentationByOffset(startOffset)

            // Replace content.
            val gather = buildString {
                append("\\begin{gather*}\n")

                equations.asSequence()
                    .map { trimEquation(it) }
                    .forEach {
                        append(indent)
                        append("    ")
                        append(it)
                        append("\\\\\n")
                    }

                append(indent)
                append("\\end{gather*}")
            }
            document.replaceString(startOffset, endOffset, gather)

            // Add import.
            PackageUtils.insertUsepackage(file, AMSMATH)
        }

        /**
         * Trims the `\[`, `\]` and whitespace from an equations.
         *
         * @param equation
         *          The equation to trim, must be display math, will not be checked by this method.
         */
        private fun trimEquation(equation: LatexNoMathContent) = equation.text
            // Remove \[ and \]
            .trimRange(2, 2)
            // Remove whitespace
            .trim()

        /**
         * Finds all adjacent equations.
         */
        private fun findEquations(base: LatexNoMathContent): List<LatexNoMathContent> {
            val equations = ArrayList<LatexNoMathContent>()
            equations.add(base)

            // Lookbehind.
            var content = base.previousSiblingIgnoreWhitespace()
            while (content is LatexNoMathContent && content.isDisplayMath()) {
                equations.add(0, content)
                content = content.previousSiblingIgnoreWhitespace()
            }

            // Lookahead.
            content = base.nextSiblingIgnoreWhitespace()
            while (content is LatexNoMathContent && content.isDisplayMath()) {
                equations.add(content)
                content = content.nextSiblingIgnoreWhitespace()
            }

            return equations
        }
    }
}