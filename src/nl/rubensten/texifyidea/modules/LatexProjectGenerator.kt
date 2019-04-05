package nl.rubensten.texifyidea.modules

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.externalSystem.model.project.settings.ConfigurationData
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.DirectoryProjectGeneratorBase
import com.intellij.platform.ProjectGeneratorPeer
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.settings.TexifySettings
import javax.swing.Icon

/**
 * This class registers the LaTeX module type especially for the creation of a new project in non-IntelliJ IDEs.
 * The creation of a new project in IntelliJ is handled by [LatexModuleType].
 *
 * At the moment of writing there seems to be no public official documentation about what needs to be done to register such a module type, but some helpful comments can be found at https://stackoverflow.com/a/49713551/4126843
 *
 * @author Thomas Schouten
 */
class LatexProjectGenerator : DirectoryProjectGeneratorBase<TexifySettings>(),
        CustomStepProjectGenerator<TexifySettings> {

    // This behaviour was inspired by the Julia plugin, see
    // https://github.com/JuliaEditorSupport/julia-intellij/blob/master/src/org/ice1000/julia/lang/module/julia-projects.kt
    override fun createStep(
            projectGenerator: DirectoryProjectGenerator<TexifySettings>,
            callback: AbstractNewProjectStep.AbstractCallback<TexifySettings>
    ) = ProjectSettingsStepBase(projectGenerator, AbstractNewProjectStep.AbstractCallback<TexifySettings>())

    override fun getName() = "LaTeX"

    override fun getDescription() = "LaTeX"

    override fun getLogo() = TexifyIcons.LATEX_MODULE!!

    override fun createPeer(): ProjectGeneratorPeer<TexifySettings> = LatexProjectGeneratorPeer()

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: TexifySettings, module: Module) {
        val modifiableModel: ModifiableRootModel = ModifiableModelsProvider.SERVICE.getInstance().getModuleModifiableModel(module)

        // Reuse the setup for IntelliJ
        LatexModuleBuilder().setupRootModel(modifiableModel)
    }

}