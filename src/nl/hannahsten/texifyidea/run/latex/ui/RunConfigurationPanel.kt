package nl.hannahsten.texifyidea.run.latex.ui

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.CollapsibleRow
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import nl.hannahsten.texifyidea.run.bibtex.BibtexRunConfigurationType
import nl.hannahsten.texifyidea.run.latex.externaltool.ExternalToolRunConfigurationType
import nl.hannahsten.texifyidea.run.makeindex.MakeindexRunConfigurationType
import java.awt.BorderLayout
import javax.swing.DefaultListSelectionModel
import javax.swing.JPanel

/**
 * @author Sten Wessel
 */
class RunConfigurationPanel(
    private val project: Project,
    private val title: String,
) : JPanel(BorderLayout()) {

    private val contentPanel = JPanel(BorderLayout())
    private val hidePanel: DialogPanel
    private var panelGroup: CollapsibleRow? = null
    private lateinit var list: JBList<RunnerAndConfigurationSettings>

    var configurations: MutableSet<RunnerAndConfigurationSettings> = mutableSetOf()
        set(value) {
            field = value
            configurationChanged()
        }

    init {

        createPanel()

        hidePanel = panel {
            collapsibleGroup(title) {
                row {
                    cell(contentPanel)
                        .align(AlignX.FILL)
                }
            }.also {
                panelGroup = it
            }
        }
        add(hidePanel, BorderLayout.CENTER)
    }

    private fun createPanel() {
        list = JBList<RunnerAndConfigurationSettings>().apply {
            emptyText.text = "No run configurations selected."
            cellRenderer = RunConfigCellRenderer(project)

            // Cell height
            prototypeCellValue = RunManagerImpl.getInstanceImpl(project).allSettings.firstOrNull()

            selectionModel = DefaultListSelectionModel()
        }

        val toolbar = ToolbarDecorator.createDecorator(list).apply {
            setToolbarPosition(ActionToolbarPosition.TOP)
            setPanelBorder(JBUI.Borders.empty())

            // No support for executing run configs in a certain order (yet)
            disableUpDownActions()

            setAddAction {
                configurations.addAll(askRunConfigurations())
                configurationChanged()
            }

            setRemoveAction {
                configurations.removeAll(list.selectedValuesList.toSet())
                configurationChanged()
            }
        }

        contentPanel.add(toolbar.createPanel(), BorderLayout.CENTER)
    }

    private fun askRunConfigurations(): List<RunnerAndConfigurationSettings> {
        val types = setOf(BibtexRunConfigurationType::class.java, MakeindexRunConfigurationType::class.java, ExternalToolRunConfigurationType::class.java)
        val configurations = RunManagerImpl.getInstanceImpl(project).allSettings.filter { it.type.javaClass in types }

        val dialog = RunConfigurationSelectionDialog(project, configurations)

        dialog.show()

        return dialog.selected
    }

    private fun changeTitle(titleSuffix: String) {
        panelGroup?.setTitle(title + titleSuffix)
    }

    private fun configurationChanged() {
        if (configurations.isNotEmpty()) {
            list.setListData(configurations.map { it }.toTypedArray())
            // Set cell height based on the size of the content
            list.visibleRowCount = configurations.size

            // Mock value change to commit changes (otherwise the apply button is not activated)
            list.setSelectionInterval(0, 0)
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
