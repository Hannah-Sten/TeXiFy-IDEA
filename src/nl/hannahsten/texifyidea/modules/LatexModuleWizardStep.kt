//package nl.hannahsten.texifyidea.modules
//
//import com.intellij.ide.util.projectWizard.ModuleWizardStep
//import java.awt.FlowLayout
//import javax.swing.JCheckBox
//import javax.swing.JComponent
//import javax.swing.JPanel
//
///**
// * @author Sten Wessel
// */
//class LatexModuleWizardStep(private val builder: LatexModuleBuilder) : ModuleWizardStep() {
//
//    private lateinit var bibtexEnabled: JCheckBox
//
//    override fun getComponent(): JComponent {
//        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
//            bibtexEnabled = JCheckBox("Configure with BibTeX support")
//            add(bibtexEnabled)
//        }
//    }
//
//    override fun updateDataModel() {
//        builder.isBibtexEnabled = bibtexEnabled.isSelected
//    }
//}
