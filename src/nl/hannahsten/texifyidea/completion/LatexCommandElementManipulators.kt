package nl.hannahsten.texifyidea.completion

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulator
import nl.hannahsten.texifyidea.psi.LatexCommands

class LatexCommandElementManipulators : ElementManipulator<LatexCommands> {

    /**
     * Return the text range of the command token, without the text range of the
     * required parameters. When this range also includes the range of the required
     * arguments, the PsiReference for custom commands gets in the way of the
     * completion inside of the required arguments.
     */
    override fun getRangeInElement(element: LatexCommands): TextRange {
        return element.commandToken.textRangeInParent
    }

    override fun handleContentChange(element: LatexCommands, range: TextRange, newContent: String?): LatexCommands? {
        return null
    }

    override fun handleContentChange(element: LatexCommands, newContent: String?): LatexCommands? {
        return null
    }
}