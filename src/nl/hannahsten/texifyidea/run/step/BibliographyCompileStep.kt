package nl.hannahsten.texifyidea.run.step

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.dialog
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.components.fields.ExtendableTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBDimension
import com.intellij.util.xmlb.annotations.Attribute
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibliographyCompiler
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.ui.compiler.CompilerEditor
import nl.hannahsten.texifyidea.util.magic.CompilerMagic


class BibliographyCompileStep(
    override val provider: CompileStepProvider, override val configuration: LatexRunConfiguration
) : CompileStep, PersistentStateComponent<BibliographyCompileStep.State> {

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
        val compilerEditor = CompilerEditor("Compiler", CompilerMagic.bibliographyCompilerByExecutableName.values).apply {
            CommonParameterFragments.setMonospaced(component)
            minimumSize = JBDimension(200, 30)
            label.isVisible = false
            component.setMinimumAndPreferredWidth(150)

            setSelectedCompiler(state.compiler)
        }

        val compilerArguments = ExpandableTextField().apply {
            emptyText.text = "Compiler arguments"
            MacrosDialog.addMacroSupport(this, MacrosDialog.Filters.ALL) { false }
            CommonParameterFragments.setMonospaced(this)

            text = state.compilerArguments
        }

        val workingDirectory = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(
                "Select Working Directory",
                "Working directory should typically be the directory where the .aux file can be found.",
                configuration.project,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
            )
            // TODO: make these actually work
            MacrosDialog.addMacroSupport(textField as ExtendableTextField, MacrosDialog.Filters.DIRECTORY_PATH) { false }

            text = state.workingDirectory ?: ""
        }

        val panel = panel {
            row("Compiler:") {
                cell {
                    component(compilerEditor.component)
                    compilerArguments(CCFlags.growX, CCFlags.pushX)
                }
            }
            row("Working directory:") {
                component(workingDirectory)
                    .comment("Working directory should typically be the directory where the .aux file can be found.")
            }
        }

        val modified = dialog(
            "Configure Bibliography Step",
            panel = panel,
            resizable = true,
            focusedComponent = compilerEditor.component,
        ).showAndGet()

        if (modified) {
            state.compiler = compilerEditor.getSelectedCompiler() as BibliographyCompiler?
            state.compilerArguments = compilerArguments.text.trim().ifEmpty { null }
            state.workingDirectory = workingDirectory.text.trim().ifEmpty { null }
        }
    }

    override fun getCommand(): List<String>? {
        TODO("Not yet implemented")
    }

    override fun getWorkingDirectory() = state.workingDirectory ?: configuration.getAuxilDirectory()?.path ?: configuration.mainFile?.parent?.path

    override fun getState() = state

    override fun loadState(state: State) {
        state.resetModificationCount()
        this.state = state
    }
}
