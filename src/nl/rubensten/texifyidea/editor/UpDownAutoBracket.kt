package nl.rubensten.texifyidea.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.psi.LatexMathContent
import nl.rubensten.texifyidea.psi.LatexMathEnvironment
import nl.rubensten.texifyidea.psi.LatexNoMathContent
import nl.rubensten.texifyidea.psi.LatexNormalText
import nl.rubensten.texifyidea.util.firstChildOfType
import nl.rubensten.texifyidea.util.hasParent
import nl.rubensten.texifyidea.util.lastChildOfType
import nl.rubensten.texifyidea.util.previousSiblingIgnoreWhitespace
import java.util.regex.Pattern

/**
 * @author Ruben Schellekens
 */
open class UpDownAutoBracket : TypedHandlerDelegate() {

    companion object {

        /**
         * Symbols that denote wheter a {} block has to be inserted when having more than 1 character.
         */
        val INSERT_SYMBOLS = setOf("_", "^")

        /**
         * Matches the suffix that denotes that braces may be inserted.
         */
        val INSERT_REQUIREMENT = Pattern.compile("^[a-zA-Z0-9]$")!!
    }

    override fun charTyped(c: Char, project: Project?, editor: Editor, file: PsiFile): Result {
        if (file.fileType != LatexFileType.INSTANCE) {
            return Result.CONTINUE
        }

        // Find selected element.
        val caret = editor.caretModel
        val element = file.findElementAt(caret.offset - 1) ?: return Result.CONTINUE

        // Insert squiggly brackets.
        if (element is LatexNormalText) {
            handleNormalText(element, editor)
        }
        else {
            val normalText = findNormalText(element)
            if (normalText != null) {
                handleNormalText(normalText, editor)
            }
        }

        return Result.CONTINUE
    }

    private fun findNormalText(element: PsiElement): LatexNormalText? {
        return exit@ when (element) {
            is PsiWhiteSpace -> {
                // Whenever the whitespace is the end of a math environment.
                val sibling = element.previousSiblingIgnoreWhitespace()
                if (sibling != null) {
                    if (sibling is LatexMathContent) {
                        return@exit sibling.lastChildOfType(LatexNoMathContent::class)
                                ?.firstChildOfType(LatexNormalText::class)
                    }
                    else {
                        return@exit sibling.firstChildOfType(LatexNormalText::class)
                    }
                }

                return@exit null
            }
            is LeafPsiElement -> {
                // Whenever a character is inserted just before the close brace of a group/inline math end.
                val content = element.prevSibling?: return@exit null
                return@exit content.firstChildOfType(LatexNormalText::class)
            }
            else -> null
        }
    }

    private fun handleNormalText(normalText: LatexNormalText, editor: Editor) {
        // Check if in math environment.
        if (!normalText.hasParent(LatexMathEnvironment::class)) {
            return
        }

        val text = normalText.text
        val offset = normalText.textOffset
        val caret = editor.caretModel
        val relative = caret.offset - normalText.textOffset

        if (relative < 3 || text.length < relative - 1) {
            return
        }

        // Only insert when a valid symbol has been typed.
        val afterSymbol = text.substring(relative - 2, relative - 1)
        if (!INSERT_REQUIREMENT.matcher(afterSymbol).matches()) {
            return
        }

        // Check if the inserted symbol is eligible for brace insertion.
        val subSupSymbol = text.substring(relative - 3, relative - 2)
        if (!INSERT_SYMBOLS.contains(subSupSymbol)) {
            return
        }

        insert(offset + relative - 3, editor)
    }

    private fun insert(symbolOffset: Int, editor: Editor) {
        val document = editor.document
        document.insertString(symbolOffset + 1, "{")
        document.insertString(symbolOffset + 4, "}")
    }
}