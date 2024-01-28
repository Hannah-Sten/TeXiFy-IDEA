package nl.hannahsten.texifyidea.editor.typedhandlers

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.psi.LatexTypes.*
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.parser.*

/**
 * In the situation $a_3<caret>$, typing 'b' should end up in $a_{32}$ as that is probably what the user intended to type.
 *
 * @author Hannah Schellekens
 */
open class UpDownAutoBracket : TypedHandlerDelegate() {

    /**
     * Symbols that denote whether a {} block has to be inserted when having more than 1 character.
     */
    private val insertSymbols = setOf("_", "^")

    /**
     * Matches the suffix that denotes that braces may be inserted.
     */
    private val insertOnly =
        """^[a-zA-Z0-9]$""".toRegex()

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (!TexifySettings.getInstance().automaticUpDownBracket) {
            return Result.CONTINUE
        }

        if (file.fileType != LatexFileType) {
            return Result.CONTINUE
        }

        // Only insert when a valid symbol has been typed.
        val afterSymbol = c.toString()
        if (!insertOnly.matches(afterSymbol)) {
            return Result.CONTINUE
        }

        // Find selected element.
        val caret = editor.caretModel
        val element = file.findElementAt(caret.offset - 1) ?: return Result.CONTINUE

        // Check if in \label.
        val parent = element.parentOfType(LatexCommands::class)
        when (parent?.name) {
            "\\label", "\\bibitem" -> return Result.CONTINUE
        }

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
        return when (element) {
            is PsiWhiteSpace -> {
                // When the whitespace is the end of a math environment.
                val sibling = element.previousSiblingIgnoreWhitespace() ?: return element.parentOfType(LatexNormalText::class)
                return when (sibling) {
                    is LatexMathContent, is LatexEnvironmentContent -> {
                        sibling.lastChildOfType(LatexNoMathContent::class)
                            ?.firstChildOfType(LatexNormalText::class)
                    }
                    is LeafPsiElement -> {
                        sibling.parentOfType(LatexNormalText::class)
                    }
                    else -> {
                        sibling.firstChildOfType(LatexNormalText::class)
                    }
                }
            }
            is PsiComment -> {
                // When for some reason people want to insert it directly before a comment.
                val mother = element.previousSiblingIgnoreWhitespace() ?: return null
                return mother.lastChildOfType(LatexNormalText::class)
            }
            is LeafPsiElement -> {
                when (element.elementType) {
                    END_COMMAND, BEGIN_COMMAND, COMMAND_TOKEN -> {
                        // When it is followed by a LatexCommands or comment tokens.
                        val noMathContent = element.firstParentOfType(LatexNoMathContent::class) ?: return null
                        val sibling = noMathContent.previousSiblingIgnoreWhitespace() ?: return null
                        return sibling.firstChildOfType(LatexNormalText::class)
                    }
                    INLINE_MATH_END -> {
                        // At the end of inline math.
                        val mathContent = element.previousSiblingIgnoreWhitespace() as? LatexMathContent ?: return null
                        val noMathContent = mathContent.lastChildOfType(LatexNoMathContent::class) ?: return null
                        return noMathContent.firstChildOfType(LatexNormalText::class)
                    }
                    else -> {
                        // When a character is inserted just before the close brace of a group/inline math end.
                        val content = element.prevSibling ?: return null
                        return content.firstChildOfType(LatexNormalText::class)
                    }
                }
            }
            else -> null
        }
    }

    private fun handleNormalText(normalText: LatexNormalText, editor: Editor) {
        // Check if in math environment.
        if (!normalText.inMathContext()) {
            return
        }

        val text = normalText.text
        val offset = normalText.textOffset
        val caret = editor.caretModel
        val relative = caret.offset - normalText.textOffset

        if (relative < 3 || text.length < relative - 1) {
            return
        }

        // Check if the inserted symbol is eligible for brace insertion.
        val subSupSymbol = text.substring(relative - 3, relative - 2)
        if (!insertSymbols.contains(subSupSymbol)) {
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
