package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.endOffset

/**
 * Implements custom folding regions.
 * See https://blog.jetbrains.com/idea/2012/03/custom-code-folding-regions-in-intellij-idea-111/
 *
 * Syntax:
 * %!<editor-fold desc="MyDescription">
 * %!</editor-fold>
 *
 * %!region MyDescription
 * %!endregion
 *
 * @author Thomas
 */
class LatexCustomFoldingBuilder : FoldingBuilderEx(), DumbAware {

    private val startRegionRegex = """%!\s*(<editor-fold desc="|region ?)(?<description>.*?)(">)?\s*$""".toRegex()
    private val endRegionRegex = """%!\s*(</editor-fold>|endregion)""".toRegex()

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode) = "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val magicComments = root.childrenOfType(LatexMagicComment::class)
        val startRegions = magicComments.filter { startRegionRegex.containsMatchIn(it.text) }
            .sortedBy { it.textOffset }.toMutableList()
        val endRegions = magicComments.filter { endRegionRegex.containsMatchIn(it.text) }
            .sortedBy { it.textOffset }

        // Now we need to match ends with starts
        for (end in endRegions) {
            // Match with the closest available start: it does not make sense to match that start with anything else, because ranges can be nested but not partially overlap
            val start = startRegions.lastOrNull { it.textOffset < end.textOffset } ?: continue
            val description = startRegionRegex.find(start.text)?.groups?.get("description")?.value?.ifBlank { "..." } ?: "..."
            startRegions.remove(start)
            descriptors.add(FoldingDescriptor(start.node, TextRange(start.textOffset, end.endOffset()), null, description))
        }

        return descriptors.toTypedArray()
    }
}
