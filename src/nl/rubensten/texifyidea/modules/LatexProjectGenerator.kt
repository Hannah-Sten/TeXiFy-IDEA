package nl.rubensten.texifyidea.modules

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.CustomStepProjectGenerator
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.project.settings.ConfigurationData
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.welcomeScreen.AbstractActionWithPanel
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.platform.DirectoryProjectGeneratorBase
import com.intellij.platform.ProjectGeneratorPeer
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.TexifyIcons
import nl.rubensten.texifyidea.settings.TexifySettings
import nl.rubensten.texifyidea.templates.LatexTemplatesFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    /** Keep a reference to the peer to get the value of the settings chosen by the user during project creation. */
    private var peer: LatexProjectGeneratorPeer? = null

    override fun createStep(
            projectGenerator: DirectoryProjectGenerator<TexifySettings>,
            callback: AbstractNewProjectStep.AbstractCallback<TexifySettings>
    ) = ProjectSettingsStepBase(projectGenerator, AbstractNewProjectStep.AbstractCallback<TexifySettings>())

    override fun getName() = "LaTeX"

    override fun getDescription() = "LaTeX"

    override fun getLogo() = TexifyIcons.LATEX_MODULE!!

    override fun createPeer(): ProjectGeneratorPeer<TexifySettings> = LatexProjectGeneratorPeer().also { peer = it }

    override fun generateProject(project: Project, baseDir: VirtualFile, settings: TexifySettings, module: Module) {

        // This behaviour was inspired by the Julia plugin, see
        // https://github.com/JuliaEditorSupport/julia-intellij/blob/master/src/org/ice1000/julia/lang/module/julia-projects.kt
        val modifiableModel: ModifiableRootModel = ModifiableModelsProvider.SERVICE.getInstance().getModuleModifiableModel(module)

        val rootModel = module.rootManager.modifiableModel

        // Behaviour inspired by LatexModuleBuilder, but it is slightly different (based on the Julia plugin)
        val fileSystem = LocalFileSystem.getInstance()

        /** Create a directory in a base directory unless it already exists. */
        fun findOrCreate(baseDir: VirtualFile, dir: String, module: Module) =
                baseDir.findChild(dir) ?: baseDir.createChildDirectory(module, dir)

        // We can only write to disk in a runWriteAction
        ApplicationManager.getApplication().runWriteAction {

            // Add source, auxil and output directories
            rootModel.contentEntries.firstOrNull()?.apply {
                addSourceFolder(findOrCreate(baseDir, "src", module), false)
                addExcludeFolder(findOrCreate(baseDir, "auxil", module))
                addExcludeFolder(findOrCreate(baseDir, "out", module))
            }

            val isBibtexEnabled: Boolean = peer?.bibtexEnabled?.isEnabled ?: false

            // Add a default LaTeX file
            val sourcePath = baseDir.path + File.separator + "src"
            addMainFile(project, sourcePath, isBibtexEnabled)

            if (isBibtexEnabled) {
                addBibFile(project, sourcePath)
            }

            rootModel.commit()

            ModifiableModelsProvider.SERVICE.getInstance().commitModuleModifiableModel(modifiableModel)
        }
    }


    // todo refactor duplicate code
    /**
     * Creates the main.tex file and applies the default file template.
     *
     * @param project
     *      The project to add the file to.
     * @param path
     *      The directory path of the file (no seperator and no file name).
     */
    private fun addMainFile(project: Project, path: String, isBibtexEnabled: Boolean) {
        val mainFilePath = path + File.separator + "main.tex"
        val mainFile = File(mainFilePath)

        // Create main file.
        try {
            mainFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Problem with creating main .tex file.", e)
        }

        // Apply template.
        val template = if (isBibtexEnabled) {
            LatexTemplatesFactory.fileTemplateTexWithBib
        } else {
            LatexTemplatesFactory.fileTemplateTex
        }
        val templateText = LatexTemplatesFactory.getTemplateText(project, template)

        try {
            FileOutputStream(mainFile).use { outputStream -> outputStream.write(templateText.toByteArray()) }
        } catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Could not apply .tex template to main file.", e)
        }

    }

    private fun addBibFile(project: Project, path: String) {
        val bibFilePath = path + File.separator + "main.bib"
        val bibFile = File(bibFilePath)

        try {
            bibFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Problem with creating main .bib file", e)
        }

        // Apply template.
        val template = LatexTemplatesFactory.fileTemplateBib
        val templateText = LatexTemplatesFactory.getTemplateText(project, template)

        try {
            FileOutputStream(bibFile).use { outputStream -> outputStream.write(templateText.toByteArray()) }
        } catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Could not apply .bib template to main bibliography file.", e)
        }
    }

}