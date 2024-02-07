package nl.hannahsten.texifyidea.modules

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.DirectoryProjectGeneratorBase
import com.intellij.platform.ProjectGeneratorPeer
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.io.File

/**
 * This class registers the LaTeX module type especially for the creation of a new project in non-IntelliJ IDEs.
 * The creation of a new project in IntelliJ is handled by [LatexModuleType].
 *
 * At the moment of writing there seems to be no public official documentation about what needs to be done to register such a module type, but some helpful comments can be found at https://stackoverflow.com/a/49713551/4126843
 *
 * @author Thomas Schouten
 */
class LatexProjectGenerator :
    DirectoryProjectGeneratorBase<TexifySettings>(),
    CustomStepProjectGenerator<TexifySettings> {

    /** Keep a reference to the peer to get the value of the settings chosen by the user during project creation. */
    private var peer: LatexProjectGeneratorPeer? = null

    override fun createStep(
        projectGenerator: DirectoryProjectGenerator<TexifySettings>,
        callback: AbstractNewProjectStep.AbstractCallback<TexifySettings>
    ) = ProjectSettingsStepBase(projectGenerator, AbstractNewProjectStep.AbstractCallback())

    override fun getName() = "LaTeX"

    override fun getDescription() = "LaTeX"

    override fun getLogo() = TexifyIcons.LATEX_MODULE

    override fun createPeer(): ProjectGeneratorPeer<TexifySettings> = LatexProjectGeneratorPeer().also { peer = it }

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: TexifySettings, module: Module) {
        // This behaviour was inspired by the Julia plugin, see
        // https://github.com/JuliaEditorSupport/julia-intellij/blob/master/src/org/ice1000/julia/lang/module/julia-projects.kt
        val modifiableModel: ModifiableRootModel = ModifiableModelsProvider.getInstance().getModuleModifiableModel(module)

        val rootModel = module.rootManager.modifiableModel

        /** Create a directory in a base directory unless it already exists. */
        fun findOrCreate(baseDir: VirtualFile, dir: String, module: Module) =
            baseDir.findChild(dir) ?: baseDir.createChildDirectory(module, dir)

        // We can only write to disk in a runWriteAction
        ApplicationManager.getApplication().runWriteAction {
            // Add source, auxil and output directories
            rootModel.contentEntries.firstOrNull()?.apply {
                addSourceFolder(findOrCreate(baseDir, "src", module), false)
                // TeX Live cannot handle an auxil/ folder
                if (LatexSdkUtil.isMiktexAvailable) {
                    addExcludeFolder(findOrCreate(baseDir, "auxil", module))
                }
                addExcludeFolder(findOrCreate(baseDir, "out", module))
            }

            val isBibtexEnabled: Boolean = peer?.bibtexEnabled?.isEnabled ?: false

            // Add a default LaTeX file
            val sourcePath = baseDir.path + File.separator + "src"
            DefaultFileCreator(project, sourcePath).addMainFile(isBibtexEnabled)

            if (isBibtexEnabled) {
                DefaultFileCreator(project, sourcePath).addBibFile()
            }

            rootModel.commit()

            ModifiableModelsProvider.getInstance().commitModuleModifiableModel(modifiableModel)
        }
    }
}