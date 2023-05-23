package nl.hannahsten.texifyidea.editor.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.psi.LatexTypes.NORMAL_TEXT_WORD
import nl.hannahsten.texifyidea.util.psi.childrenOfType
import nl.hannahsten.texifyidea.util.shiftRight
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * Folding symbols that are not escaped, like en dashes.
 * Similar to [LatexEscapedSymbolFoldingBuilder].
 */
class LatexSymbolFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun isCollapsedByDefault(node: ASTNode) = true

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("SymbolFoldingGroup")
        val file = root.containingFile

        // Fold all hyphens in the document
        return file.childrenOfType<LatexNormalText>().flatMap { it.node.getChildren(TokenSet.create(NORMAL_TEXT_WORD)).toSet() }
            .filter { "--" in it.text }
            .flatMap { node ->
                val text = node.text
                "-{2,}".toRegex().findAll(text).map {
                    val range = it.range.shiftRight(node.startOffset).toTextRange()
                    FoldingDescriptor(node, range, group, getPlaceholderText(it.value))
                }
            }
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
