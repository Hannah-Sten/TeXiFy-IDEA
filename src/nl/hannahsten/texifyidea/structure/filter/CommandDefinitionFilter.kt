package nl.hannahsten.texifyidea.structure.filter

import com.intellij.ide.util.FileStructureFilter
import com.intellij.ide.util.treeView.smartTree.ActionPresentation
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.openapi.actionSystem.Shortcut
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.structure.latex.LatexStructureViewCommandElement
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * @author Hannah Schellekens
 */
class CommandDefinitionFilter : FileStructureFilter {

    override fun isVisible(treeElement: TreeElement): Boolean =
        treeElement !is LatexStructureViewCommandElement ||
            !(
                treeElement.commandName in CommandMagic.commandDefinitionsAndRedefinitions ||
                    treeElement.commandName in CommandMagic.packageInclusionCommands
                )

    override fun isReverted() = true

    override fun getPresentation() = LatexNewCommandFilterPresentation

    override fun getName() = "latex.texify.filter.newcommand"

    override fun getCheckBoxText() = "Command definitions"

    override fun getShortcut() = emptyArray<Shortcut>()

    /**
     * @author Hannah Schellekens
     */
    object LatexNewCommandFilterPresentation : ActionPresentation {

        override fun getText() = "Show Command Definitions"

        override fun getDescription() = "Show command definitions"

        override fun getIcon() = TexifyIcons.DOT_COMMAND
    }
}
