package nl.hannahsten.texifyidea.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Project-level settings UI.
 *
 * The project settings are a property so they will differ per project.
 */
class TexifyProjectConfigurable(private val projectSettings: TexifyProjectSettings) : SearchableConfigurable {
    private lateinit var compilerCompatibility: ComboBox<LatexCompiler>

    override fun getId() = "TexifyProjectConfigurable"

    override fun getDisplayName() = "Project Settings"

    override fun createComponent() = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            compilerCompatibility = addCompilerCompatibility()

        })
    }

    /**
     * Add the options for the compiler compatibility.
     */
    private fun JPanel.addCompilerCompatibility(): ComboBox<LatexCompiler> {
        // Show available compilers
        val list = ComboBox(LatexCompiler.values())
        add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JBLabel("Check for compatibility with compiler: "))
            add(list)
        })
        return list
    }

    override fun isModified(): Boolean {
        return compilerCompatibility.selectedItem != projectSettings.compilerCompatibility
    }

    override fun apply() {
        projectSettings.compilerCompatibility = compilerCompatibility.selectedItem as LatexCompiler
    }

    override fun reset() {
        super.reset()
        compilerCompatibility.selectedItem = projectSettings.compilerCompatibility
    }
}