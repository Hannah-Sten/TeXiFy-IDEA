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
import nl.hannahsten.texifyidea.util.childrenOfType

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
        val descriptors = ArrayList<FoldingDescriptor>()
        val entries = root.childrenOfType(BibtexEntry::class)

        for (entry in entries) {
            val start = entry.textOffset
            val end = entry.endtry.textOffset + 1 // 1 for the last bracket

            if (end <= start) {
                continue
            }

            descriptors.add(FoldingDescriptor(entry, TextRange(start, end)))
        }

        return descriptors.toTypedArray()
    }
}
