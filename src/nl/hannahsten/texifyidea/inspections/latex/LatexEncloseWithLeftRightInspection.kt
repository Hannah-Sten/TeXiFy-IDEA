package nl.hannahsten.texifyidea.inspections.latex

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
import nl.hannahsten.texifyidea.inspections.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyLineOptionsInspection
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexInlineMath
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class LatexEncloseWithLeftRightInspection : TexifyLineOptionsInspection("Custom commands") {

    companion object {

        private val brackets = mapOf(
            "(" to ")",
            "[" to "]"
        )
    }

    override val inspectionGroup: InsightGroup
        get() = InsightGroup.LATEX

    override val inspectionId = "EncloseWithLeftRight"

    override val outerSuppressionScopes = EnumSet.of(MagicCommentScope.COMMAND)!!

    override fun getDisplayName() = "Enclose high lines with \\leftX..\\rightX"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()
        val document = file.document() ?: return descriptors

        val mathModes = file.childrenOfType(LatexInlineMath::class) + file.childrenOfType(LatexDisplayMath::class)
        for (mathMode in mathModes) {
            // Scan all characters in math mode. (+2/-2 to ignore \[ and \]).
            for (openOffset in mathMode.textOffset + 2 until mathMode.endOffset() - 2) {
                val char = document[openOffset]
                if (!brackets.containsKey(char)) {
                    continue
                }
                if (ignore(document, openOffset)) {
                    continue
                }

                val closeOffset = seek(document, openOffset, file) ?: continue
                val openElement = file.findElementAt(openOffset) ?: continue
                val closeElement = file.findElementAt(closeOffset) ?: continue

                descriptors.add(
                    manager.createProblemDescriptor(
                        openElement,
                        TextRange.from(0, 1),
                        "Parentheses pair could be replaced by \\left(..\\right)",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        InsertLeftRightFix(openOffset, closeOffset, document[openOffset])
                    )
                )

                descriptors.add(
                    manager.createProblemDescriptor(
                        closeElement,
                        TextRange.from(0, 1),
                        "Parentheses pair could be replaced by \\left(..\\right)",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOntheFly,
                        InsertLeftRightFix(openOffset, closeOffset, document[openOffset])
                    )
                )
            }
        }

        return descriptors
    }

    private fun ignore(document: Document, offset: Int): Boolean {
        if (document.textLength < 6 || offset + 6 >= document.textLength) {
            return false
        }

        // Lookbehind for \left or \right.
        if (offset - 6 >= 0) {
            if (document[offset - 5 until offset] == "\\left" || document[offset - 6 until offset] == "\\right") {
                return true
            }
        }

        return false
    }

    /**
     * Check if the command that starts at 'offset' is a command which takes up vertical space, such that it should be enclosed with left./right.
     */
    private fun hasAffectedCommand(document: Document, offset: Int): Boolean {
        for (cmd in affectedCommands()) {
            if (offset + cmd.length <= document.textLength) {
                val found = document[offset until offset + cmd.length]
                if (found == cmd) {
                    return true
                }
            }
        }

        return false
    }

    private fun seek(document: Document, offset: Int, file: PsiFile): Int? {
        // Scan document.
        val open = document[offset]
        val close = brackets[open]

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

    private fun affectedCommands() = CommandMagic.high + lines

    /**
     * @author Hannah Schellekens
     */
    private open class InsertLeftRightFix(val openOffset: Int, val closeOffset: Int, val open: String) : LocalQuickFix {

        override fun getFamilyName() = "Convert (..) to \\left(..\\right)"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile
            val document = file.document() ?: return

            document[closeOffset] = "\\right${brackets[open]}"
            document[openOffset] = "\\left$open"
        }
    }
}