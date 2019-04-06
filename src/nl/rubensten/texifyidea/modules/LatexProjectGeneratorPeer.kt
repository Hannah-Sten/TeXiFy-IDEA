package nl.rubensten.texifyidea.modules

import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.GeneratorPeerImpl
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.platform.WebProjectGenerator
import nl.rubensten.texifyidea.settings.TexifySettings
import java.awt.FlowLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * todo https://github.com/intellij-rust/intellij-rust/blob/master/src/main/kotlin/org/rust/ide/newProject/RsProjectGeneratorPeer.kt
 * https://github.com/JuliaEditorSupport/julia-intellij/blob/master/src/org/ice1000/julia/lang/module/ui/ui-impl.kt
 */
class LatexProjectGeneratorPeer : ProjectGeneratorPeer<TexifySettings> {

    private lateinit var bibtexEnabled: JCheckBox

    private val settings = TexifySettings()
    private val listeners = ArrayList<ProjectGeneratorPeer.SettingsListener>()

    override fun validate(): Nothing? = null

    override fun getSettings() = settings

    /** Deprecated but we have to override it. */
    @Deprecated("", ReplaceWith("addSettingsListener"), level = DeprecationLevel.ERROR)
    override fun addSettingsStateListener(@Suppress("DEPRECATION") listener: com.intellij.platform.WebProjectGenerator.SettingsStateListener) = Unit


    override fun buildUI(settingsStep: SettingsStep) = settingsStep.addExpertPanel(component)

    override fun isBackgroundJobRunning() = false

    override fun addSettingsListener(listener: ProjectGeneratorPeer.SettingsListener) {
        listeners += listener
    }

    override fun getComponent(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            bibtexEnabled = JCheckBox("Configure with BibTeX support")

            // todo In order to retrieve the value when creating project files, save to settings? But then it will never be updated again? How do other projects do this?
//            bibtexEnabled.addActionListener {
//
//            }
            add(bibtexEnabled)
        }
    }

    // todo save the setting?
//    override fun updateDataModel() {
//        builder.isBibtexEnabled = bibtexEnabled.isSelected
//    }
}