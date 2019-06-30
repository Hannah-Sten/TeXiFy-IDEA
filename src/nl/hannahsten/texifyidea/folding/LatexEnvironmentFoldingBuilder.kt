package nl.hannahsten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.childrenOfType
import java.util.*

/**
 * Adds folding regions for LaTeX environments.
 *
 * Enables folding of `\begin{environment} ... \end{environment}`.
 *
 * @author Sten Wessel
 */
class LatexEnvironmentFoldingBuilder : FoldingBuilderEx() {

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode) = "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val environments = root.childrenOfType(LatexEnvironment::class)

        for (environment in environments) {
            // Get content offsets.
            // Uses the commands instead of the actual contents as they may be empty.
            val start = environment.beginCommand.textRange.endOffset
            val end = environment.endCommand.textRange.startOffset

            if (end <= start) {
                continue
            }

            descriptors.add(FoldingDescriptor(environment, TextRange(start, end)))
        }

        return descriptors.toTypedArray()
    }
}
