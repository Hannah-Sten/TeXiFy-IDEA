package nl.hannahsten.texifyidea.settings.sdk

import com.intellij.codeInsight.daemon.ProjectSdkSetupValidator
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.SdkPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.ui.EditorNotificationPanel
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.util.runCommand

/**
 * https://jetbrains.org/intellij/sdk/docs/reference_guide/project_model/sdk.html#assisting-in-setting-up-an-sdk
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
        val module = ModuleUtilCore.findModuleForFile(file, project) ?: return null
        if (ModuleRootManager.getInstance(module).sdk?.sdkType is LatexSdk || ProjectRootManager.getInstance(project).projectSdk?.sdkType is LatexSdk) return null
        return "No LaTeX installation could be found. Please add it to PATH or set up a LaTeX SDK (and reopen this file)."
    }

    override fun getFixHandler(project: Project, file: VirtualFile): EditorNotificationPanel.ActionHandler {
        return SdkPopupFactory.newBuilder().withProject(project)
            .withSdkTypeFilter { id: SdkTypeId -> id is LatexSdk }
            .updateProjectSdkFromSelection()
            .buildEditorNotificationPanelHandler()
    }
}