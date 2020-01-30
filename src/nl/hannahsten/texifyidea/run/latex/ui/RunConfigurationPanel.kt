package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.ui.AnActionButton
import com.intellij.ui.HideableTitledPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.util.IconUtil
import java.awt.BorderLayout
import javax.swing.DefaultListSelectionModel
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class RunConfigurationPanel<RunConfigurationType : ConfigurationType>(
        private val project: Project,
        private val title: String,
        private val runConfigurationType: Class<RunConfigurationType>) : JPanel(BorderLayout()) {

    private val contentPanel = JPanel(BorderLayout())
    private val hidePanel: HideableTitledPanel
    private lateinit var list: JBList<RunnerAndConfigurationSettings>

    var configurations: List<RunnerAndConfigurationSettings?> = emptyList()
        set(value) {
            field = value
            configurationChanged()
        }

    init {

        createPanel()
        hidePanel = HideableTitledPanel(title, false).apply {
            setContentComponent(contentPanel)
        }
        add(hidePanel, BorderLayout.CENTER)
    }

    private fun createPanel() {
        list = JBList<RunnerAndConfigurationSettings>().apply {
//            visibleRowCount = 1
            emptyText.text = "No run configurations selected."
            cellRenderer = RunConfigCellRenderer(project)

            // Cell height
            prototypeCellValue = RunManagerImpl.getInstanceImpl(project).allSettings.firstOrNull()

            // Disable selection
//            selectionModel = object : DefaultListSelectionModel() {
//                override fun setSelectionInterval(index0: Int, index1: Int) {
//                    super.setSelectionInterval(-1, -1)
//                    fireValueChanged(-1, -1, false)
//                }
//            }
        }

        val toolbar = ToolbarDecorator.createDecorator(list).apply {
            setAsUsualTopToolbar()

            disableUpDownActions()
            disableRemoveAction()

            setAddIcon(IconUtil.getEditIcon())
            setAddAction {
                configurations = askRunConfigurations()
            }

            val removeAction = object : AnActionButton("Remove", IconUtil.getRemoveIcon()) {

                override fun isEnabled() = !list.isEmpty

                override fun actionPerformed(e: AnActionEvent) {
                    configurations = emptyList()
                }
            }

            addExtraAction(removeAction)
        }

        contentPanel.add(toolbar.createPanel(), BorderLayout.CENTER)
    }

    private fun askRunConfigurations(): List<RunnerAndConfigurationSettings> {
        val configurations = RunManagerImpl.getInstanceImpl(project).allSettings.filter { it.type.javaClass == runConfigurationType  }

        val dialog = RunConfigurationSelectionDialog(project, configurations)

        dialog.show()

        return dialog.selected
    }

    private fun changeTitle(titleSuffix: String) {
        hidePanel.title = title + titleSuffix
    }

    private fun configurationChanged() {
        if (configurations.isNotEmpty()) {
            list = JBList(configurations.mapNotNull { it })

            // Mock value change to commit changes (otherwise the apply button is not activated)
            list.setSelectionInterval(-1, -1)
            changeTitle("Enabled")
            return
        }

        list.setListData(emptyArray())

        // Mock value change to commit changes (otherwise the apply button is not activated)
        list.setSelectionInterval(-1, -1)

        changeTitle("Disabled")
    }
}
