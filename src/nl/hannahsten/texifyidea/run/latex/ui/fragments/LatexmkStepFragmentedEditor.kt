package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBTextField
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.StepUiOptionIds
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCitationTool
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import java.awt.event.ItemEvent

internal class LatexmkStepFragmentedEditor(
    initialStep: LatexmkCompileStepOptions = LatexmkCompileStepOptions(),
) : AbstractStepFragmentedEditor<LatexmkCompileStepOptions>(initialStep) {

    private val compilerPath = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(true, false, false, false, false, false)
                    .withTitle(TexifyBundle.message("run.step.ui.dialog.choose.compiler.executable"))
            )
        )
    }
    private val compilerPathRow = LabeledComponent.create(compilerPath, TexifyBundle.message("run.step.ui.field.compiler.path.label"))

    private val latexmkCompileMode = ComboBox(LatexmkCompileMode.entries.toTypedArray())
    private val latexmkCompileModeRow = LabeledComponent.create(latexmkCompileMode, TexifyBundle.message("run.step.ui.field.latexmk.compile.mode.label"))

    private val latexmkCustomEngineCommand = JBTextField()
    private val latexmkCustomEngineRow = LabeledComponent.create(latexmkCustomEngineCommand, TexifyBundle.message("run.step.ui.field.latexmk.custom.engine.command.label"))

    private val latexmkCitationTool = ComboBox(LatexmkCitationTool.entries.toTypedArray())
    private val latexmkCitationToolRow = LabeledComponent.create(latexmkCitationTool, TexifyBundle.message("run.step.ui.field.latexmk.citation.tool.label"))

    private val latexmkExtraArguments = RawCommandLineEditor().apply {
        editorField.emptyText.text = TexifyBundle.message("run.latexmk.settings.additional.arguments")
    }
    private val latexmkExtraArgumentsRow = LabeledComponent.create(latexmkExtraArguments, TexifyBundle.message("run.step.ui.field.latexmk.extra.arguments.label"))

    init {
        latexmkCompileMode.addItemListener {
            if (it.stateChange != ItemEvent.SELECTED) {
                return@addItemListener
            }
            latexmkCustomEngineCommand.isEnabled = latexmkCompileMode.selectedItem == LatexmkCompileMode.CUSTOM
            if (!latexmkCustomEngineCommand.isEnabled) {
                latexmkCustomEngineCommand.text = ""
            }
            fireEditorStateChanged()
        }
    }

    override fun createFragments(): Collection<SettingsEditorFragment<LatexmkCompileStepOptions, *>> {
        val headerFragment = CommonParameterFragments.createHeader<LatexmkCompileStepOptions>(TexifyBundle.message("run.step.ui.header.latexmk"))

        val pathFragment = stepFragment(
            id = StepUiOptionIds.COMPILE_PATH,
            name = TexifyBundle.message("run.step.ui.field.compiler.path"),
            component = compilerPathRow,
            reset = { step, component -> component.component.text = step.compilerPath.orEmpty() },
            apply = { step, component -> step.compilerPath = component.component.text.ifBlank { null } },
            initiallyVisible = { step -> !step.compilerPath.isNullOrBlank() },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.latexmk.compiler.path"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.custom.compiler.path"),
        )

        val modeFragment = stepFragment(
            id = StepUiOptionIds.LATEXMK_MODE,
            name = TexifyBundle.message("run.step.ui.field.latexmk.compile.mode"),
            component = latexmkCompileModeRow,
            reset = { step, component -> component.component.selectedItem = step.latexmkCompileMode },
            apply = { step, component ->
                step.latexmkCompileMode = component.component.selectedItem as? LatexmkCompileMode ?: LatexmkCompileMode.AUTO
            },
            initiallyVisible = { true },
            removable = false,
            hint = TexifyBundle.message("run.step.ui.hint.latexmk.compile.mode"),
        )

        val customEngineFragment = stepFragment(
            id = StepUiOptionIds.LATEXMK_CUSTOM_ENGINE,
            name = TexifyBundle.message("run.step.ui.field.latexmk.custom.engine.command"),
            component = latexmkCustomEngineRow,
            reset = { step, component ->
                component.component.text = step.latexmkCustomEngineCommand.orEmpty()
                component.component.isEnabled = step.latexmkCompileMode == LatexmkCompileMode.CUSTOM
            },
            apply = { step, component ->
                step.latexmkCustomEngineCommand = component.component.text
            },
            initiallyVisible = { step ->
                step.latexmkCompileMode == LatexmkCompileMode.CUSTOM || !step.latexmkCustomEngineCommand.isNullOrBlank()
            },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.latexmk.custom.engine.command"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.latexmk.custom.engine.command"),
        )

        val citationFragment = stepFragment(
            id = StepUiOptionIds.LATEXMK_CITATION,
            name = TexifyBundle.message("run.step.ui.field.latexmk.citation.tool"),
            component = latexmkCitationToolRow,
            reset = { step, component -> component.component.selectedItem = step.latexmkCitationTool },
            apply = { step, component ->
                step.latexmkCitationTool = component.component.selectedItem as? LatexmkCitationTool ?: LatexmkCitationTool.AUTO
            },
            initiallyVisible = { step -> step.latexmkCitationTool != LatexmkCitationTool.AUTO },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.latexmk.citation.tool"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.latexmk.citation.tool"),
        )

        val extraArgsFragment = stepFragment(
            id = StepUiOptionIds.LATEXMK_EXTRA_ARGS,
            name = TexifyBundle.message("run.step.ui.field.latexmk.extra.arguments"),
            component = latexmkExtraArgumentsRow,
            reset = { step, component -> component.component.text = step.latexmkExtraArguments.orEmpty() },
            apply = { step, component -> step.latexmkExtraArguments = component.component.text },
            initiallyVisible = { step ->
                !step.latexmkExtraArguments.isNullOrBlank() &&
                    step.latexmkExtraArguments != LatexRunConfiguration.DEFAULT_LATEXMK_EXTRA_ARGUMENTS
            },
            removable = true,
            hint = TexifyBundle.message("run.step.ui.hint.latexmk.extra.arguments"),
            actionHint = TexifyBundle.message("run.step.ui.action.set.latexmk.extra.arguments"),
        )

        return listOf(
            headerFragment,
            modeFragment,
            pathFragment,
            customEngineFragment,
            citationFragment,
            extraArgsFragment,
        )
    }
}
