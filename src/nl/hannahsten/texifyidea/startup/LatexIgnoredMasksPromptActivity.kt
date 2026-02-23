package nl.hannahsten.texifyidea.startup

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.files.LatexIgnoredFileMasks
import nl.hannahsten.texifyidea.util.isLatexProject

class LatexIgnoredMasksPromptActivity : ProjectActivity, DumbAware {

    override suspend fun execute(project: Project) {
        if (!shouldPrompt(project)) return

        Notification(
            "LaTeX",
            "TeXiFy: Ignore LaTeX intermediate files?",
            "TeXiFy can add ignored file masks for LaTeX intermediate files. Existing ignored rules will be kept.",
            NotificationType.INFORMATION,
        )
            .addAction(
                NotificationAction.createSimpleExpiring("Apply now") {
                    val mergedMasks = LatexIgnoredFileMasks.mergeWithPreset(LatexIgnoredFileMasks.getCurrentMasks())
                    LatexIgnoredFileMasks.applyMasks(mergedMasks)
                }
            )
            .addAction(
                NotificationAction.createSimpleExpiring("Not now") {}
            )
            .addAction(
                NotificationAction.createSimpleExpiring("Don't ask again") {
                    TexifySettings.getState().suppressIgnoredMasksPrompt = true
                }
            )
            .notify(project)
    }

    internal suspend fun shouldPrompt(project: Project): Boolean {
        val isLatex = readAction {
            project.isLatexProject()
        }
        if (!isLatex) return false
        if (TexifySettings.getState().suppressIgnoredMasksPrompt) return false
        return LatexIgnoredFileMasks.findMissingMasks(LatexIgnoredFileMasks.getCurrentMasks()).isNotEmpty()
    }
}
