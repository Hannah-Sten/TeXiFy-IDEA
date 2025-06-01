package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexRecursiveIgnoreTextVisitor

/**
 * @author Sten Wessel
 */
class LatexMathSymbolFoldingBuilder : FoldingBuilderEx(), DumbAware {

    /*
    Improve the performance by ignoring whether the command
     */

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val visitor = FoldingVisitor()
        root.accept(visitor)
        return visitor.descriptors.toTypedArray()
    }

    inner class FoldingVisitor : LatexRecursiveIgnoreTextVisitor() {
        val descriptors = mutableListOf<FoldingDescriptor>()

        /**
         * Folds math commands if they are defined in [LatexMathCommand].
         */
        override fun visitCommands(o: LatexCommands) {
            val name = o.name ?: return
            val mathCommand = LatexMathCommand.getWithSlash(name) ?: return
            val display = mathCommand.first().display ?: return
            val cmdToken = o.commandToken
            descriptors.add(FoldingDescriptor(cmdToken.node, cmdToken.textRange, null, display))
            o.acceptChildren(this)
        }
    }

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().foldMathSymbols

    override fun getPlaceholderText(node: ASTNode): String? = null
}
