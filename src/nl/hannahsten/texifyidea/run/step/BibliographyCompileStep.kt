package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.dialog
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.panel
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.run.bibtex.compiler.BibliographyCompiler
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration


class BibliographyCompileStep(
    override val provider: LatexCompileStepProvider, override val configuration: LatexRunConfiguration
) : LatexCompileStep, PersistentStateComponent<BibliographyCompileStep.State> {

    class State : BaseState() {

        @get:Attribute("compiler", converter = BibliographyCompiler.Converter::class)
        var compiler by property<BibliographyCompiler?>(null) { it == null }

        @get:Attribute()
        var compilerArguments by string()

        @get:Attribute()
        var workingDirectory by string()
    }

    private var state = State()


    override fun configure() {
        // TODO: cancel: reset state?

//        val compilerModel = SortedComboBoxModel<BibliographyCompiler>(compareBy { it.displayName }).apply {
//            addAll(CompilerMagic.)
//        }

        val panel = panel {
            row("Compiler:") {
//                comboBox()
//                CommonParameterFragments.setMonospaced()
            }
            row("Compiler arguments:") {
                this.
                component(RawCommandLineEditor().apply {
                    // TODO: make these actually work
                    MacrosDialog.addMacroSupport(editorField, MacrosDialog.Filters.ALL) { false }
                    CommonParameterFragments.setMonospaced(textField)
                })
            }
            row("Working directory:") {
                component(TextFieldWithBrowseButton().apply {
                    addBrowseFolderListener(
                        "Select Working Directory",
                        "Working directory should typically be the directory where the .aux file can be found",
                        configuration.project,
                        FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                        TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
                    )
                    // TODO: make these actually work
                    MacrosDialog.addMacroSupport(textField as ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }
                })
            }
        }

        dialog(
            "Configure Bibliography Step",
            panel = panel,
            resizable = true,
            focusedComponent = null,
        ).show()
    }

    override fun execute() {
        TODO("Not yet implemented")
    }

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }
}
