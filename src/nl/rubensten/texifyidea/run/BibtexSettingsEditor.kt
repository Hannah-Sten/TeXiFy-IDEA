package nl.rubensten.texifyidea.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.VerticalFlowLayout
import nl.rubensten.texifyidea.run.compiler.BibliographyCompiler
import javax.swing.JComponent
import javax.swing.JPanel

/**
 *
 * @author Sten Wessel
 */
class BibtexSettingsEditor(project: Project) : SettingsEditor<BibtexRunConfiguration>() {

    private lateinit var panel: JPanel
    private lateinit var compiler: LabeledComponent<ComboBox<BibliographyCompiler>>

    override fun createEditor(): JComponent {
        createUIComponents();
        return panel;
    }

    override fun resetEditorFrom(runConfig: BibtexRunConfiguration) {
        compiler.component.selectedItem = runConfig.compiler
    }

    override fun applyEditorTo(runConfig: BibtexRunConfiguration) {
        runConfig.compiler = compiler.component.selectedItem as BibliographyCompiler
    }

    private fun createUIComponents() {
        panel = JPanel().apply {
            layout = VerticalFlowLayout(VerticalFlowLayout.TOP)

            // Compiler
            val compilerField = ComboBox<BibliographyCompiler>(BibliographyCompiler.values())
            compiler = LabeledComponent.create(compilerField, "Compiler")
            add(compiler)
        }
    }

}
