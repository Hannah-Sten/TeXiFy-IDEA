package nl.hannahsten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.util.childrenOfType

/**
 * @author Sten Wessel
 */
class LatexMathSymbolFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = listOf<FoldingDescriptor>().toMutableList()
        val mathEnvironments = root.childrenOfType(LatexMathEnvironment::class)

        for (mathEnvironment in mathEnvironments) {
            val group = FoldingGroup.newGroup("latexMathSymbol")
            val commands = mathEnvironment.childrenOfType(LatexCommands::class)

            for (command in commands) {
                val display = LatexMathCommand[command.commandToken.text.substring(1)]?.first()?.display ?: continue

                descriptors.add(object : FoldingDescriptor(command.commandToken.node, command.commandToken.textRange, group) {
                    override fun getPlaceholderText() = display
                })
            }
        }

        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode): String? = null
}
