package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ui.configuration.SdkPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.util.runCommand

/**
 * Validates that a LaTeX SDK is configured for the project or module.
 * Shows an editor notification if no LaTeX SDK is found and pdflatex is not in PATH.
 *
 * This validator supports both project-level and module-level SDK configuration.
 * A module-level SDK takes precedence over the project-level SDK.
 *
 * @author Thomas
 */
class LatexProjectSdkSetupValidator : ProjectSdkSetupValidator {

    object Cache {
        val isPdflatexInPath by lazy {
            for (retry in 0..5) {
                runCommand("pdflatex", "--version", timeout = 10)?.let {
                    return@lazy it.contains("pdfTeX")
                }
            }
            false
        }
    }

    override fun isApplicableFor(project: Project, file: VirtualFile): Boolean {
        // Check if setting up a LaTeX SDK would make sense
        return file.fileType is LatexFileType || PsiManager.getInstance(project).findFile(file)?.language?.isKindOf(LatexLanguage) == true
    }

    override fun getErrorMessage(project: Project, file: VirtualFile): String? {
        // Based on https://github.com/rikvdkleij/intellij-haskell/blob/895a214b174b69f661d4f7d4230633058fca8f1e/src/main/scala/intellij/haskell/notification/HaskellProjectSdkSetupValidator.scala#L24
        // Nothing to do if we don't need an SDK
        if (Cache.isPdflatexInPath) return null

        // Use the centralized SDK lookup which checks module SDK first, then project SDK
        if (LatexSdkUtil.getLatexSdkForFile(file, project) != null) return null

        return "No LaTeX installation could be found. Please add it to PATH or set up a LaTeX SDK (and reopen this file)."
    }

    override fun getFixHandler(project: Project, file: VirtualFile): EditorNotificationPanel.ActionHandler = SdkPopupFactory.newBuilder().withProject(project)
        .withSdkTypeFilter { id: SdkTypeId -> id is LatexSdk }
        .updateProjectSdkFromSelection()
        .buildEditorNotificationPanelHandler()
}