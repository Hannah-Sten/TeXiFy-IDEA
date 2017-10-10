package nl.rubensten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.inspections.TexifyLineOptionsInspection
import nl.rubensten.texifyidea.psi.LatexDisplayMath
import nl.rubensten.texifyidea.psi.LatexInlineMath
import nl.rubensten.texifyidea.util.*
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * @author Ruben Schellekens
 */
open class LatexEncloseWithLeftRightInspection : TexifyLineOptionsInspection("Custom commands") {

    companion object {

        private val AFFECTED_COMMANDS = setOf(
                "\\frac", "\\dfrac", "\\sqrt", "\\sum", "\\int", "\\iint", "\\iiint", "\\iiiint",
                "\\prod", "\\bigcup", "\\bigcap", "\\bigsqcup", "\\bigsqcap"
        )

        val BRACKETS = mapOf(
                "(" to ")",
                "[" to "]"
        )
    }

    override fun getInspectionGroup() = InsightGroup.LATEX

    override fun getDisplayName() = "Enclose high lines with \\leftX..\\rightX"

    override fun getInspectionId() = "EncloseWithLeftRight"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()
        val document = file.document() ?: return descriptors

        val mathModes = file.childrenOfType(LatexInlineMath::class) + file.childrenOfType(LatexDisplayMath::class)
        for (mathMode in mathModes) {
            // Scan all characters in math mode.
            for (openOffset in mathMode.textOffset until mathMode.endOffset()) {
                val char = document[openOffset]
                if (!BRACKETS.containsKey(char)) {
                    continue
                }
                if (ignore(document, openOffset)) {
                    continue
                }

                val closeOffset = seek(document, openOffset, file) ?: continue
                val openElement = file.findElementAt(openOffset) ?: continue
                val closeElement = file.findElementAt(closeOffset) ?: continue

                descriptors.add(manager.createProblemDescriptor(
                        openElement,
                        TextRange.from(0, 1),
                        "Parentheses pair could be replaced by \\left(..\\right)",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        InsertLeftRightFix(openOffset, closeOffset, document[openOffset])
                ))

                descriptors.add(manager.createProblemDescriptor(
                        closeElement,
                        TextRange.from(0, 1),
                        "Parentheses pair could be replaced by \\left(..\\right)",
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly,
                        InsertLeftRightFix(openOffset, closeOffset, document[openOffset])
                ))
            }
        }

        return descriptors
    }

    private fun ignore(document: Document, offset: Int): Boolean {
        if (document.textLength < 6 || offset + 6 >= document.textLength ) {
            return false
        }

        // Lookbehind for \left or \right.
        if (document[offset - 5 until offset]  == "\\left" || document[offset - 6 until offset]  == "\\right") {
            return true
        }

        return false
    }

    private fun hasAffectedCommand(document: Document, offset: Int): Boolean {
        for (cmd in affectedCommands()) {
            val found = document[offset until offset + cmd.length]
            if (found == cmd) {
                return true
            }
        }

        return false
    }

    private fun seek(document: Document, offset: Int, file: PsiFile): Int? {
        // Scan document.
        val open = document[offset]
        val close = BRACKETS[open]

        var current = offset
        var nested = 0
        var closeOffset: Int? = null
        var foundAffectedCommand = false

        while (++current < file.textLength) {
            val char = document[current]

            // Ignore comments.
            val element = file.findElementAt(current)
            if (element is PsiComment) {
                continue
            }

            if (!element!!.inMathContext() && element !is PsiWhiteSpace) {
                break
            }

            // Check if it is part of a \left or \right.
            if (ignore(document, current)) {
                continue
            }

            // Check if there is an affected command.
            if (char == "\\" && hasAffectedCommand(document, current)) {
                foundAffectedCommand = true
            }

            // Open nesting
            if (char == open) {
                nested++
                continue
            }

            // Close nesting
            if (char == close && nested > 0) {
                nested--
                continue
            }

            // Whenever met at correct closure
            if (char == close && nested <= 0) {
                closeOffset = current
                break
            }
        }

        if (!foundAffectedCommand || closeOffset == null) {
            return null
        }

        return closeOffset
    }

    private fun affectedCommands(): Set<String> {
        return AFFECTED_COMMANDS + lines
    }

    /**
     * @author Ruben Schellekens
     */
    private open class InsertLeftRightFix(val openOffset: Int, val closeOffset: Int, val open: String) : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Convert (..) to \\left(..\\right)"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile
            val document = file.document() ?: return

            document[closeOffset] = "\\right${BRACKETS[open]}"
            document[openOffset] = "\\left$open"
        }
    }
}