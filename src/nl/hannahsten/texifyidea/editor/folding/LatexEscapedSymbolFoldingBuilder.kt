package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.util.parser.traverseCommands

/**
 * @author Johannes Berger
 */
class LatexEscapedSymbolFoldingBuilder : FoldingBuilderEx(), DumbAware {
    private val commandsToFold = setOf("%", "#", "&", "_", "$").map { "\\" + it }

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().foldEscapedSymbols

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
//        val group = FoldingGroup.newGroup("EscapedSymbolFoldingGroup")
//        val file = root.containingFile
//
//        return file.commandsInFile()
//            .filter { it.commandToken.text in commandsToFold }
//            .map { FoldingDescriptor(it.node, it.textRange, group, it.commandToken.text.substringAfter("\\")) }
//            .toTypedArray()
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.traverseCommands {
            val text = it.commandToken.text
            if(text in commandsToFold) {
                descriptors.add(FoldingDescriptor(it.node, it.textRange, null, text.substring(1)))
            }
        }
        return descriptors.toTypedArray()
    }
}
