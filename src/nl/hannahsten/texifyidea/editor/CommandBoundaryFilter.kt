package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.editor.actions.WordBoundaryFilter
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.psi.LatexTypes

class CommandBoundaryFilter : WordBoundaryFilter() {
    override fun isWordBoundary(previousTokenType: IElementType, tokenType: IElementType): Boolean {
        if (previousTokenType == LatexTypes.BACKSLASH && tokenType == LatexTypes.COMMAND_TOKEN) {
            return false
        }
        return super.isWordBoundary(previousTokenType, tokenType)
    }
}