package nl.hannahsten.texifyidea.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexTypes.DASH
import nl.hannahsten.texifyidea.util.childrenOfType

/**
 * Folding symbols that are not escaped, like en dashes.
 * Similar to [LatexEscapedSymbolFoldingBuilder].
 */
class LatexSymbolFoldingBuilder : FoldingBuilderEx() {

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("SymbolFoldingGroup")
        val file = root.containingFile

        // Get all hyphens in the document
        return file.childrenOfType<LatexNormalText>().flatMap { it.node.getChildren(TokenSet.create(DASH)).toSet() }
            .map { FoldingDescriptor(it, it.textRange, group, getPlaceholderText(it.text)) }
            .toTypedArray()
    }

    private fun getPlaceholderText(string: String): String {
        return when (string) {
            // The following are covered by the DASH token type
            "-" -> "-" // hyphen
            "--" -> "–" // en dash
            "---" -> "—" // em dash
            else -> string
        }
    }
}
