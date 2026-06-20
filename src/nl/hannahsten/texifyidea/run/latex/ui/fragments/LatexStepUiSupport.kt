package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.FragmentedSettings
import com.intellij.icons.AllIcons
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.step.LatexStepPresentation
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepProviders
import javax.swing.Icon

internal object LatexStepUiSupport {

    fun availableStepTypes(): List<String> = LatexRunStepProviders.all.map { it.type }

    fun description(type: String): String = LatexStepPresentation.displayName(type)

    fun icon(type: String): Icon? = when {
        LatexRunStepProviders.find(type) != null -> null
        else -> AllIcons.General.Warning
    }
}

internal fun LatexStepRunConfigurationOptions.copyWithIdentity(
    stepId: String,
    selectedOptions: List<FragmentedSettings.Option> = this.selectedOptions,
): LatexStepRunConfigurationOptions = deepCopy().also {
    it.id = stepId
    it.selectedOptions = selectedOptions
        .map { option -> FragmentedSettings.Option(option.name ?: "", option.visible) }
        .toMutableList()
}
