package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.icons.AllIcons
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
