package nl.rubensten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import nl.rubensten.texifyidea.index.LatexIncludesIndex
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexContent
import nl.rubensten.texifyidea.psi.PsiContainer
import nl.rubensten.texifyidea.util.Magic
import nl.rubensten.texifyidea.util.firstChildOfType
import nl.rubensten.texifyidea.util.parentOfType

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

    private fun PsiElement.nextCommand(): LatexCommands? {
        val content = if (this is LatexCommands) {
            parentOfType(LatexContent::class) ?: return null
        }
        else this
        val next = content.nextSibling

        // When having multiple breaks, don't find new commands to fold.
        // When whitespace without multiple breaks, look further.
        if (next is PsiWhiteSpace) {
            return if (Magic.Pattern.containsMultipleNewlines.matcher(next.text).matches()) {
                null
            }
            else next.nextCommand()
        }

        // No next? Then there is no valid next command. This happens e.g. when it's the last element of the file.
        next ?: return null

        // Skip comments.
        if (next is PsiComment) {
            return next.nextCommand()
        }

        return next.firstChildOfType(LatexCommands::class)
    }
}