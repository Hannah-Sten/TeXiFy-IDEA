package nl.rubensten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.index.LatexCommandsIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.PsiContainer
import nl.rubensten.texifyidea.util.nextSiblingIgnoreWhitespace
import nl.rubensten.texifyidea.util.parentOfType
import nl.rubensten.texifyidea.util.previousSiblingIgnoreWhitespace

/**
 * Folds multiple \\usepackage or \\RequirePackage statements
 *
 * @author Ruben Schellekens
 */
open class LatexSectionFoldingBuilder : FoldingBuilderEx() {

    companion object {

        private val sectionCommands = arrayOf("\\section", "\\chapter")
    }

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode) = node.text + "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val commands = LatexCommandsIndex.getCommandsByNames(root.containingFile, *sectionCommands).toList()

        if (commands.isEmpty()) {
            return descriptors.toTypedArray()
        }

        // Fold all sections that end with another section.
        var previous = commands.first()
        for (command in commands) {
            if (previous == command) {
                continue
            }

            val end = command.parentOfType(LatexContent::class)?.previousSiblingIgnoreWhitespace() ?: continue
            if (end.textOffset < previous.textOffset) {
                continue
            }

            val elt = PsiContainer(previous, end)
            descriptors.add(FoldingDescriptor(elt, elt.textRange))
            previous = command
        }

        // Find the range of the last section.
        findEnd(descriptors, previous)

        return descriptors.toTypedArray()
    }

    private fun findEnd(descriptors: MutableCollection<FoldingDescriptor>, lastCommand: LatexCommands) {
        val content = lastCommand.parentOfType(LatexContent::class) ?: return
        var next: LatexContent? = content
        var previous: LatexContent = content
        while (next != null) {
            previous = next
            next = next.nextSiblingIgnoreWhitespace() as? LatexContent
        }

        val elt = PsiContainer(content, previous)
        descriptors.add(FoldingDescriptor(elt, elt.textRange))
    }
}