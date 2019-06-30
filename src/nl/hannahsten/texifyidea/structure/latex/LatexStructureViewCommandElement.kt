package nl.hannahsten.texifyidea.structure.latex

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.NavigationItem
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.structure.EditableHintPresentation
import nl.hannahsten.texifyidea.util.nextCommand
import java.util.*

/**
 * @author Hannah Schellekens
 */
class LatexStructureViewCommandElement(private val element: LatexCommands) : StructureViewTreeElement, SortableTreeElement {

    companion object {

        @JvmStatic
        fun newCommand(commands: LatexCommands): LatexStructureViewCommandElement? {
            if ("\\let" == commands.commandToken.text || "\\def" == commands.commandToken.text) {
                val sibling = commands.nextCommand() ?: return null

                return LatexStructureViewCommandElement(sibling)
            }

            return LatexStructureViewCommandElement(commands)
        }
    }

    private val sectionChildren = ArrayList<TreeElement>()
    private val presentation = LatexPresentationFactory.getPresentation(element)

    val commandName: String
        get() = element.commandToken.text

    fun addChild(child: TreeElement) {
        sectionChildren.add(child)
    }

    fun setHint(hint: String) {
        (presentation as? EditableHintPresentation)?.setHint(hint)
    }

    override fun getValue() = element

    override fun getAlphaSortKey(): String {
        val text = presentation.presentableText
        return text ?: ""
    }

    override fun getPresentation() = presentation

    override fun getChildren() = sectionChildren.toTypedArray()

    override fun navigate(requestFocus: Boolean) {
        if (element is NavigationItem) {
            (element as NavigationItem).navigate(requestFocus)
        }
    }

    override fun canNavigate(): Boolean {
        return element is NavigationItem && (element as NavigationItem).canNavigate()
    }

    override fun canNavigateToSource(): Boolean {
        return element is NavigationItem && (element as NavigationItem).canNavigateToSource()
    }
}
