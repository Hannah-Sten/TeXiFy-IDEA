package nl.rubensten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.index.LatexIncludesIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.PsiContainer
import nl.rubensten.texifyidea.util.nextCommand

/**
 * Folds multiple \\usepackage or \\RequirePackage statements
 *
 * @author Ruben Schellekens
 */
open class LatexImportFoldingBuilder : FoldingBuilderEx() {

    companion object {

        private val includesSet = setOf("\\usepackage", "\\RequirePackage")
        private val includesArray = includesSet.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode) = "\\usepackage{...}"

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val covered = HashSet<LatexCommands>()
        val commands = LatexIncludesIndex.getCommandsByNames(root.containingFile, *includesArray)

        for (command in commands) {
            // Do not cover commands twice.
            if (command in covered) {
                continue
            }

            // Iterate over all consecutive commands.
            var next: LatexCommands? = command
            var last: LatexCommands = command
            while (next != null && next.name in includesSet) {
                covered += next
                last = next
                next = next.nextCommand()
            }

            val elt = PsiContainer(command, last)
            descriptors.add(FoldingDescriptor(elt, elt.textRange))
        }

        return descriptors.toTypedArray()
    }
}