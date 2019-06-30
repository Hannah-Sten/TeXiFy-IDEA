package nl.hannahsten.texifyidea.modules

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.CompilerModuleExtension
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.util.*

/**
 * @author Hannah Schellekens, Sten Wessel
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
                DefaultFileCreator(project, path).addMainFile(isBibtexEnabled)
                if (isBibtexEnabled) {
                    DefaultFileCreator(project, path).addBibFile()
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
            DefaultFileCreator(project, path).addMainFile(isBibtexEnabled)
            if (isBibtexEnabled) {
                DefaultFileCreator(project, path).addBibFile()
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
}