package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRequiredParam
import nl.hannahsten.texifyidea.util.childrenOfType
import nl.hannahsten.texifyidea.util.firstChildOfType
import nl.hannahsten.texifyidea.util.magic.CommandMagic.foldableFootnotes

/**
 * Adds folding regions for LaTeX environments.
 *
 * Enables folding of `\footnote{}`.
 *
 * @author jojo2357
 */
class LatexFootnoteFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode): String {
        val parsedText = node.text.substring(1).trim()
        return if (parsedText.length > 8) parsedText.substring(0, 8) + "..." else parsedText.substring(0, parsedText.length - 1)
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val parameters =
            root.childrenOfType(LatexCommands::class).filter { foldableFootnotes.contains(it.name) }.mapNotNull {
                it.firstChildOfType(LatexRequiredParam::class)
            }

        for (environment in parameters) {
            if (environment.endOffset - 1 > environment.startOffset + 1)
                descriptors.add(FoldingDescriptor(environment.originalElement, TextRange(environment.startOffset + 1, environment.endOffset - 1)))
        }

        return descriptors.toTypedArray()
    }
}
