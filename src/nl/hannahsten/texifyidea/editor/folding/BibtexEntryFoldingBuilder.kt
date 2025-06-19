package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.BibtexTypes
import nl.hannahsten.texifyidea.util.parser.forEachDirectChild

/**
 * Adds folding regions for BibTeX entries.
 *
 * @author Thomas Schouten
 */
class BibtexEntryFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun isCollapsedByDefault(node: ASTNode) = false

    override fun getPlaceholderText(node: ASTNode): String {
        val type = node.findChildByType(BibtexTypes.TYPE)?.text
        val id = node.findChildByType(BibtexTypes.ID)?.text
        return if (type == null || id == null) {
            "..."
        }
        else {
            "$type{$id ...}"
        }
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        // bibtexFile - entry
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.forEachDirectChild {
            if(it is BibtexEntry) {
                val start = it.textOffset
                val end = it.textRange.endOffset

                if (end > start) {
                    descriptors.add(FoldingDescriptor(it, TextRange(start, end)))
                }
            }
        }
        return descriptors.toTypedArray()
    }
}
