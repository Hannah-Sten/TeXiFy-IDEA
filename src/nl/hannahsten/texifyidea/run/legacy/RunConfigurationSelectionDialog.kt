package nl.hannahsten.texifyidea.run.legacy

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

/**
 * Adapted from [com.intellij.execution.impl.RunConfigurationBeforeRunProvider]
 *
 * @author Sten Wessel
 */
class RunConfigurationSelectionDialog(
    private val project: Project,
    private val settings: List<RunnerAndConfigurationSettings>,
    selected: List<RunnerAndConfigurationSettings> = emptyList()
) : DialogWrapper(project) {

    private lateinit var list: JBList<RunnerAndConfigurationSettings>

    var selected = selected
        get() = if (isOK) field else emptyList()

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
            val maxWidth = 24 + (
                settings.asSequence().map { fontMetrics.stringWidth(it.configuration.name) }.maxOrNull()
                    ?: (fontMetrics.stringWidth("m") * 30)
                )

            minimumSize = Dimension(maxWidth, preferredSize.height)
        }
    }

    override fun createCenterPanel(): JComponent {
        list = JBList(settings).apply {
            selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION

            selectionModel.addListSelectionListener {
                selected = list.selectedValuesList
                isOKActionEnabled = !list.selectedValuesList.isNullOrEmpty()
            }

            cellRenderer = RunConfigCellRenderer(project)
        }

        return JBScrollPane(list)
    }
}