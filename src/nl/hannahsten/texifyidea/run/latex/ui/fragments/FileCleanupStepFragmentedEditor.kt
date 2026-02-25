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

    private val description = JPanel(BorderLayout()).apply {
        add(JBLabel("No additional settings. This step removes queued temporary files and empty directories."), BorderLayout.CENTER)
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
            hint = "Cleanup removes files queued by compile and auxiliary steps.",
        )

        return listOf(
            header,
            descriptionFragment,
        )
    }
}
