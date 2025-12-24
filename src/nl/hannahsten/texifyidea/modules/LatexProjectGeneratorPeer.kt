package nl.hannahsten.texifyidea.modules

import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.ui.components.JBCheckBox
import nl.hannahsten.texifyidea.settings.TexifySettings
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Customize the project creation dialog, adding settings.
 *
 * Inspired by the Rust plugin, source:
 * https://github.com/intellij-rust/intellij-rust/blob/master/src/main/kotlin/org/rust/ide/newProject/RsProjectGeneratorPeer.kt
 */
class LatexProjectGeneratorPeer : ProjectGeneratorPeer<TexifySettings> {

    lateinit var bibtexEnabled: JBCheckBox

    private val settings = TexifySettings()
    private val listeners = ArrayList<ProjectGeneratorPeer.SettingsListener>()

    override fun validate(): Nothing? = null

    override fun getSettings() = settings

    override fun buildUI(settingsStep: SettingsStep) = settingsStep.addExpertPanel(component)

    override fun isBackgroundJobRunning() = false

    override fun addSettingsListener(listener: ProjectGeneratorPeer.SettingsListener) {
        listeners += listener
    }

    @Deprecated("Deprecated in Java")
    override fun getComponent(): JComponent = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        bibtexEnabled = JBCheckBox("Configure with BibTeX support")
        add(bibtexEnabled)
    }
}
