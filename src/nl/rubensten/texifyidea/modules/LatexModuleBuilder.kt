package nl.rubensten.texifyidea.modules

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import nl.rubensten.texifyidea.TeXception
import nl.rubensten.texifyidea.templates.LatexTemplatesFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * @author Ruben Schellekens, Sten Wessel
 */
class LatexModuleBuilder : ModuleBuilder() {

    private val sourcePaths: List<Pair<String, String>>
        get() {
            val paths = ArrayList<Pair<String, String>>()
            val path = contentEntryPath + File.separator + "src"
            File(path).mkdirs()
            paths.add(Pair.create(path, ""))

            return paths
        }

    var isBibtexEnabled = false

    override fun getModuleType() = LatexModuleType.INSTANCE

    override fun getCustomOptionsStep(context: WizardContext?, parentDisposable: Disposable?) = LatexModuleWizardStep(this)

    @Throws(ConfigurationException::class)
    override fun setupRootModel(rootModel: ModifiableRootModel) {
        val project = rootModel.project
        val fileSystem = LocalFileSystem.getInstance()
        val compilerModuleExtension = rootModel.getModuleExtension(CompilerModuleExtension::class.java)
        compilerModuleExtension.isExcludeOutput = true

        val contentEntry = doAddContentEntry(rootModel) ?: return

        for (sourcePath in sourcePaths) {
            val path = sourcePath.first
            File(path).mkdirs()
            val sourceRoot = fileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))

            if (sourceRoot != null) {
                contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second)
                addMainFile(project, path)
                if (isBibtexEnabled) {
                    addBibFile(project, path)
                }
            }
        }

        // Create source directory.
        for (sourcePath in sourcePaths) {
            val path = sourcePath.first
            File(path).mkdirs()

            val fileName = FileUtil.toSystemIndependentName(path)
            val sourceRoot = fileSystem.refreshAndFindFileByPath(fileName) ?: continue

            contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second)
            addMainFile(project, path)
            if (isBibtexEnabled) {
                addBibFile(project, path)
            }
            fileSystem.refresh(true)
        }

        // Create output directory.
        var path = contentEntryPath + File.separator + "out"
        File(path).mkdirs()
        val outRoot = fileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))
        if (outRoot != null) {
            contentEntry.addExcludeFolder(outRoot)
        }

        // Create auxiliary directory.
        path = contentEntryPath + File.separator + "auxil"
        File(path).mkdirs()
        val auxRoot = fileSystem.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(path))
        if (auxRoot != null) {
            contentEntry.addExcludeFolder(auxRoot)
        }
    }

    /**
     * Creates the main.tex file and applies the default file template.
     *
     * @param project
     *      The project to add the file to.
     * @param path
     *      The directory path of the file (no seperator and no file name).
     */
    private fun addMainFile(project: Project, path: String) {
        val mainFilePath = path + File.separator + "main.tex"
        val mainFile = File(mainFilePath)

        // Create main file.
        try {
            mainFile.createNewFile()
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Problem with creating main .tex file.", e)
        }

        // Apply template.
        val template = if (isBibtexEnabled) {
            LatexTemplatesFactory.fileTemplateTexWithBib
        }
        else {
            LatexTemplatesFactory.fileTemplateTex
        }
        val templateText = LatexTemplatesFactory.getTemplateText(project, template)

        try {
            FileOutputStream(mainFile).use { outputStream -> outputStream.write(templateText.toByteArray()) }
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Could not apply .tex template to main file.", e)
        }

    }

    private fun addBibFile(project: Project, path: String) {
        val bibFilePath = path + File.separator + "main.bib"
        val bibFile = File(bibFilePath)

        try {
            bibFile.createNewFile()
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Problem with creating main .bib file", e)
        }

        // Apply template.
        val template = LatexTemplatesFactory.fileTemplateBib
        val templateText = LatexTemplatesFactory.getTemplateText(project, template)

        try {
            FileOutputStream(bibFile).use { outputStream -> outputStream.write(templateText.toByteArray()) }
        }
        catch (e: IOException) {
            e.printStackTrace()
            throw TeXception("Could not apply .bib template to main bibliography file.", e)
        }
    }
}