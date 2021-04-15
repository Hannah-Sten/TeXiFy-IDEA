package nl.hannahsten.texifyidea.run.legacy

import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

/**
 * @author Sten Wessel
 */
class RunConfigCellRenderer(private val project: Project) : ColoredListCellRenderer<RunnerAndConfigurationSettings>() {

    override fun customizeCellRenderer(
        list: JList<out RunnerAndConfigurationSettings>,
        settings: RunnerAndConfigurationSettings?, index: Int,
        selected: Boolean, hasFocus: Boolean
    ) {
        icon = RunManagerEx.getInstanceEx(project).getConfigurationIcon(settings ?: return)
        append(
            settings.configuration.name,
            if (settings.isTemporary) SimpleTextAttributes.GRAY_ATTRIBUTES
            else SimpleTextAttributes.REGULAR_ATTRIBUTES
        )
        append(" " + settings.configuration.type.displayName, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
    }
}