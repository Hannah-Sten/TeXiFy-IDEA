package nl.rubensten.texifyidea.run

import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

/**
 * Adapted from [com.intellij.execution.impl.RunConfigurationBeforeRunProvider.SelectionDialog]
 *
 * @author Sten Wessel
 */
class RunConfigurationSelectionDialog(
    private val project: Project,
    private val settings: List<RunnerAndConfigurationSettings>,
    selected: RunnerAndConfigurationSettings? = null
) : DialogWrapper(project) {

    private lateinit var list: JBList<RunnerAndConfigurationSettings>

    var selected = selected
        get() = if (isOK) field else null


    init {
        title = "Choose Run Configuration"
        init()

        list.apply {
            setSelectedValue(selected, true)

            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (SwingUtilities.isLeftMouseButton(e) && e?.clickCount == 2) {
                        doOKAction()
                    }
                }
            })

            val fontMetrics = list.getFontMetrics(list.font)

            // Icon and gap (= 24) + max size of the configuration names
            val maxWidth = 24 + (settings.map { fontMetrics.stringWidth(it.configuration.name) }.max()
                ?: fontMetrics.stringWidth("m") * 30)

            minimumSize = Dimension(maxWidth, preferredSize.height)
        }
    }

    override fun createCenterPanel(): JComponent? {
        list = JBList(settings).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION

            selectionModel.addListSelectionListener { _ ->
                selected = list.selectedValue
                isOKActionEnabled = list.selectedValue != null
            }

            cellRenderer = object : ColoredListCellRenderer<RunnerAndConfigurationSettings>() {
                override fun customizeCellRenderer(list: JList<out RunnerAndConfigurationSettings>, settings: RunnerAndConfigurationSettings?, index: Int, selected: Boolean, hasFocus: Boolean) {
                    icon = RunManagerEx.getInstanceEx(project).getConfigurationIcon(settings ?: return)
                    append(
                        settings.configuration.name,
                        if (settings.isTemporary)
                            SimpleTextAttributes.GRAY_ATTRIBUTES
                        else
                            SimpleTextAttributes.REGULAR_ATTRIBUTES
                    )
                }
            }
        }

        return JBScrollPane(list)
    }

}
