package nl.rubensten.texifyidea.run

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.ui.HideableTitledPanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

/**
 *
 * @author Sten Wessel
 */
class BibliographyPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val panel = JPanel()

    init {
        createPanel()
        val hidePanel = HideableTitledPanel("Bibliography", true).apply {
            setContentComponent(panel)
        }
        add(hidePanel, BorderLayout.CENTER)
    }

    private fun createPanel() {
        panel.add(JButton("Select configuration").apply {
            addActionListener {
                text = askRunConfiguration()?.name ?: ""
            }
        })
    }

    private fun askRunConfiguration(): RunnerAndConfigurationSettings? {
        val configurations = RunManagerImpl.getInstanceImpl(project).allSettings.filter { it.type is BibtexRunConfigurationType }
        val dialog = RunConfigurationSelectionDialog(project, configurations)

        dialog.show()

        return dialog.selected
    }
}
