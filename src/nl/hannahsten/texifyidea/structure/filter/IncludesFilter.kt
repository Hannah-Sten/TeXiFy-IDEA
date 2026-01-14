package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.FileStructureFilter
import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.openapi.actionSystem.Shortcut
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement

/**
 * @author Hannah Schellekens
 */
class IncludesFilter : FileStructureFilter {

    override fun isVisible(treeElement: TreeElement): Boolean = if (treeElement !is LatexStructureViewCommandElement) {
        true
    }
    else !treeElement.isFileInclude

    override fun isReverted() = true

    override fun getPresentation() = LatexIncludesFilterPresentation

    override fun getName() = "latex.texify.filter.includes"

    override fun getCheckBoxText() = "Include commands"

    override fun getShortcut() = emptyArray<Shortcut>()

    /**
     * @author Hannah Schellekens
     */
    object LatexIncludesFilterPresentation : ActionPresentation {

        override fun getText() = "Show Includes"

        override fun getDescription() = "Show includes"

        override fun getIcon() = TexifyIcons.DOT_INCLUDE
    }
}
