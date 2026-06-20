package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.ui.components.JBLabel
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import java.awt.BorderLayout
import javax.swing.JPanel

internal class FileCleanupStepFragmentedEditor(
    initialStep: FileCleanupStepOptions = FileCleanupStepOptions(),
) : AbstractStepFragmentedEditor<FileCleanupStepOptions>(initialStep) {

    companion object {

        internal const val DESCRIPTION_TEXT = "Removes temporary build artifacts"

        internal const val DESCRIPTION_HINT =
            "Cleanup removes temporary build artifacts while preserving final outputs."
    }

    private val description = JPanel(BorderLayout()).apply {
        add(JBLabel(DESCRIPTION_TEXT), BorderLayout.CENTER)
    }

    override fun createFragments(): Collection<SettingsEditorFragment<FileCleanupStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<FileCleanupStepOptions>("File Cleanup Step")
        val descriptionFragment = stepFragment(
            id = "step.cleanup.description",
            name = "Description",
            component = description,
            reset = { _, _ -> },
            apply = { _, _ -> },
            initiallyVisible = { true },
            removable = false,
            hint = DESCRIPTION_HINT,
        )

        return listOf(
            header,
            descriptionFragment,
        )
    }
}
