package nl.rubensten.texifyidea.modules

import com.intellij.platform.GeneratorPeerImpl
import nl.rubensten.texifyidea.settings.TexifySettings
import java.awt.FlowLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * todo https://github.com/intellij-rust/intellij-rust/blob/master/src/main/kotlin/org/rust/ide/newProject/RsProjectGeneratorPeer.kt
 */
class LatexProjectGeneratorPeer : GeneratorPeerImpl<TexifySettings>() {
    private lateinit var bibtexEnabled: JCheckBox

    override fun getComponent(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            bibtexEnabled = JCheckBox("Configure with BibTeX support")
            add(bibtexEnabled)
        }
    }

    // todo save the setting?
//    override fun updateDataModel() {
//        builder.isBibtexEnabled = bibtexEnabled.isSelected
//    }
}