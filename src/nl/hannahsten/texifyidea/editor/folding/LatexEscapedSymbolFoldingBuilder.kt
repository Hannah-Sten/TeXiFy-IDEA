package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.util.files.commandsInFile

/**
 * @author Johannes Berger
 */
class LatexEscapedSymbolFoldingBuilder : FoldingBuilderEx() {

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("EscapedSymbolFoldingGroup")
        val file = root.containingFile

        return file.commandsInFile()
            .filter { it.commandToken.text in commandsToFold }
            .map { FoldingDescriptor(it.node, it.textRange, group, it.commandToken.text.substringAfter("\\")) }
            .toTypedArray()
    }

    companion object {

        val commandsToFold = setOf("%", "#", "&", "_", "$").map { "\\" + it }
    }
}
