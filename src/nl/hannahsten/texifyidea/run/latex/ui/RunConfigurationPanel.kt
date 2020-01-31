package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.ui.HideableTitledPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
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

    var configurations: MutableSet<RunnerAndConfigurationSettings> = mutableSetOf()
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

    // todo after adding run config, need to click on it to enable apply button
    // todo apply button also activated when just selecting a run config
    private fun createPanel() {
        list = JBList<RunnerAndConfigurationSettings>().apply {
            emptyText.text = "No run configurations selected."
            cellRenderer = RunConfigCellRenderer(project)

            // Cell height
            prototypeCellValue = RunManagerImpl.getInstanceImpl(project).allSettings.firstOrNull()

            selectionModel = DefaultListSelectionModel()
        }

        val toolbar = ToolbarDecorator.createDecorator(list).apply {
            setAsUsualTopToolbar()

            disableUpDownActions()

            setAddAction {
                configurations.addAll(askRunConfigurations())
                configurationChanged()
            }

            setRemoveAction {
                configurations.removeAll(list.selectedValuesList)
                configurationChanged()
            }
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
            list.setListData(configurations.map { it }.toTypedArray() )
            // Set cell height based on the size of the content
            list.visibleRowCount = configurations.size

            // Mock value change to commit changes (otherwise the apply button is not activated)
            list.setSelectionInterval(-1, -1)
            changeTitle("Enabled")
            return
        }

        list.setListData(emptyArray())
        list.visibleRowCount = 1

        // Mock value change to commit changes (otherwise the apply button is not activated)
        list.setSelectionInterval(-1, -1)

        changeTitle("Disabled")
    }
}
