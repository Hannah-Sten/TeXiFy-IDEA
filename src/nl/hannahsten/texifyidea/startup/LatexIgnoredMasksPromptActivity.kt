package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.LatexIgnoredFileMasks
import nl.hannahsten.texifyidea.util.isLatexProject

class LatexIgnoredMasksPromptActivity : ProjectActivity, DumbAware {

    override suspend fun execute(project: Project) {
        if (!shouldApply(project)) return

        val mergedMasks = LatexIgnoredFileMasks.mergeWithPreset(LatexIgnoredFileMasks.getCurrentMasks())
        LatexIgnoredFileMasks.applyMasks(mergedMasks)
    }

    internal suspend fun shouldApply(project: Project): Boolean {
        if (!TexifySettings.getState().autoApplyIgnoredLatexMasks) return false

        val isLatex = readAction {
            project.isLatexProject()
        }
        if (!isLatex) return false

        return LatexIgnoredFileMasks.findMissingMasks(LatexIgnoredFileMasks.getCurrentMasks()).isNotEmpty()
    }
}
