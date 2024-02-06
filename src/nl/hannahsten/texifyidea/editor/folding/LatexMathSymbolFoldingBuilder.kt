package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.lang.commands.LatexMathCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironmentContent
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.inMathContext

/**
 * @author Sten Wessel
 */
class LatexMathSymbolFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = listOf<FoldingDescriptor>().toMutableList()
        val inlineOrDisplayMath = root.childrenOfType(LatexMathEnvironment::class)
        val mathEnvironments = root.childrenOfType(LatexEnvironmentContent::class).filter { it.inMathContext() }

        for (mathEnvironment in inlineOrDisplayMath + mathEnvironments) {
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

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().foldMathSymbols

    override fun getPlaceholderText(node: ASTNode): String? = null
}
