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
import nl.hannahsten.texifyidea.util.parser.childrenOfType
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
import nl.hannahsten.texifyidea.util.parser.traverse
import nl.hannahsten.texifyidea.util.shiftRight
import nl.hannahsten.texifyidea.util.toTextRange

/**
 * Folding symbols that are not escaped, like en dashes.
 * Similar to [LatexEscapedSymbolFoldingBuilder].
 */
class LatexSymbolFoldingBuilder : FoldingBuilderEx(), DumbAware {

    val dashRegex = "-{2,}".toRegex()

    override fun isCollapsedByDefault(node: ASTNode) = LatexCodeFoldingSettings.getInstance().foldSymbols

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("SymbolFoldingGroup")
        val file = root.containingFile
        val tokenFilter = TokenSet.create(NORMAL_TEXT_WORD)

        // Fold all hyphens in the document
        val descriptors = mutableListOf<FoldingDescriptor>()

        file.traverse {
            if (it is LatexNormalText) {
                for(child in it.node.getChildren(tokenFilter)){
                    val text = child.text
                    dashRegex.findAll(text).forEach { matchResult ->
                        val range = matchResult.range.shiftRight(child.startOffset).toTextRange()
                        descriptors.add(FoldingDescriptor(child, range, group, getPlaceholderText(matchResult.value)))
                    }
                }
            }
            true
        }
        return descriptors.toTypedArray()
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
