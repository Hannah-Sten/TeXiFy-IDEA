package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

/**
 * Custom renderer for the LaTeX Distribution ComboBox that shows:
 * - For "SDK of main file" / "Project SDK": the resolved SDK name with secondary label in gray
 * - For concrete distribution types: the distribution name
 */
class LatexDistributionComboBoxRenderer(
    private val project: Project,
    private val mainFileProvider: () -> VirtualFile?
) : ColoredListCellRenderer<LatexDistributionSelection>() {

    override fun customizeCellRenderer(
        list: JList<out LatexDistributionSelection>,
        value: LatexDistributionSelection?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        if (value == null) return

        val mainFile = mainFileProvider()
        val displayName = value.getDisplayName(mainFile, project)
        val secondaryLabel = value.secondaryLabel

        if (secondaryLabel != null) {
            // SDK_FROM_MAIN_FILE or PROJECT_SDK - show resolved name and secondary label
            if (displayName.startsWith("<")) {
                // No SDK configured
                append(displayName, SimpleTextAttributes.ERROR_ATTRIBUTES)
            }
            else {
                append(displayName)
            }
            append(" ")
            append(secondaryLabel, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
        else {
            // Concrete distribution type - just show the name
            append(displayName)
        }
    }
}
