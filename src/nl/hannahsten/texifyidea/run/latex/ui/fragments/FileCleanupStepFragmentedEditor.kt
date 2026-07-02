package nl.hannahsten.texifyidea.run.latex.ui.fragments

import com.intellij.execution.ui.CommonParameterFragments
import com.intellij.execution.ui.SettingsEditorFragment
import com.intellij.ui.components.JBLabel
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import java.awt.BorderLayout
import javax.swing.JPanel

internal class FileCleanupStepFragmentedEditor(
    initialStep: FileCleanupStepOptions = FileCleanupStepOptions(),
) : AbstractStepFragmentedEditor<FileCleanupStepOptions>(initialStep) {

    private val description = JPanel(BorderLayout()).apply {
        add(JBLabel(TexifyBundle.message("run.step.ui.file.cleanup.description")), BorderLayout.CENTER)
    }

    override fun createFragments(): Collection<SettingsEditorFragment<FileCleanupStepOptions, *>> {
        val header = CommonParameterFragments.createHeader<FileCleanupStepOptions>(TexifyBundle.message("run.step.ui.header.file.cleanup"))
        val descriptionFragment = stepFragment(
            id = "step.cleanup.description",
            name = TexifyBundle.message("run.step.ui.field.description"),
            component = description,
            reset = { _, _ -> },
            apply = { _, _ -> },
            initiallyVisible = { true },
            removable = false,
            hint = TexifyBundle.message("run.step.ui.hint.file.cleanup.description"),
        )

        return listOf(
            header,
            descriptionFragment,
        )
    }
}
