package nl.hannahsten.texifyidea.run.options

import com.intellij.execution.ExecutionException
import com.intellij.ide.macro.ProjectFileDirMacro
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.run.compiler.latex.LatexCompiler
import nl.hannahsten.texifyidea.util.files.FileUtil
import nl.hannahsten.texifyidea.util.files.createExcludedDir
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import java.io.File
import java.nio.file.Path
import java.util.*

/**
 * Output or auxiliary path of the run configuration.
 */
abstract class LatexRunConfigurationAbstractOutputPathOption(override val pathWithMacro: String?, override val resolvedPath: String?) : LatexRunConfigurationAbstractPathOption(pathWithMacro, resolvedPath) {

    companion object {

        /**
         * Get default output path based on variant (out or auxil), includes macro.
         *
         * @param project If null, then the resolvedPath will be null.
         */
        fun getDefault(variant: String, project: Project?): LatexRunConfigurationOutputPathOption {
            // See docs of projectFile
            // todo because of this problem, consider using ProjectPathMacro
            val projectDir = if (project?.projectFile?.parent?.path?.endsWith(".idea") == true) project.projectFile?.parent?.parent else project?.projectFile?.parent

            // Add project file dir to context and resolve the macro with it (it's unclear why the key is not already in the context)
            val context = SimpleDataContext.builder().add(PlatformDataKeys.PROJECT_FILE_DIRECTORY, projectDir).build()
            val defaultWithMacro = "\$${ProjectFileDirMacro().name}\$/$variant"
            val resolved = ProjectFileDirMacro().expand(context) + "/$variant"
            return LatexRunConfigurationOutputPathOption(resolved, defaultWithMacro)
        }
    }

    override fun isDefault(): Boolean {
        return pathWithMacro == null && resolvedPath == null
    }

    fun isDefault(variant: String): Boolean {
        return pathWithMacro == getDefault(variant, null).pathWithMacro
    }

    /**
     * Get path to output file (e.g. pdf)
     */
    fun getOutputFilePath(options: LatexRunConfigurationOptions, project: Project): String {
        val outputDir = getOrCreateOutputPath(options.mainFile.resolve(), project)
        return "${outputDir?.path}/" + options.mainFile.resolve()
            ?.nameWithoutExtension + "." + if (options.outputFormat == LatexCompiler.OutputFormat.DEFAULT) "pdf"
        else options.outputFormat.toString()
            .lowercase(Locale.getDefault())
    }

    /**
     * Tries to resolve the [resolvedPath], and if it doesn't exist, tries to create it.
     */
    fun getOrCreateOutputPath(mainFile: VirtualFile?, project: Project): VirtualFile? {
        val outPath = resolvedPath ?: return null
        val resolved = resolve()
        if (resolved != null) return resolved

        // Can be improved by assuming a relative path to the project, using given context
        if (outPath.isBlank() || !Path.of(outPath).isAbsolute || mainFile == null) return null
        val fileIndex = ProjectRootManager.getInstance(project).fileIndex

        // Create output path for non-MiKTeX systems (MiKTeX creates it automatically)
        val module = fileIndex.getModuleForFile(mainFile, false)
        if (File(outPath).mkdirs()) {
            module?.createExcludedDir(outPath)
            return LocalFileSystem.getInstance().refreshAndFindFileByPath(outPath)
        }
        return null
    }

    /**
     * If the output path is the same as the directory the main file is in.
     */
    fun isMainFileParent(mainFile: VirtualFile?, project: Project): Boolean {
        return getOrCreateOutputPath(mainFile, project) == mainFile?.parent
    }

    /**
     * Copy subdirectories of the source directory to the output directory for includes to work in non-MiKTeX systems
     */
    @Throws(ExecutionException::class)
    fun updateOutputSubDirs(mainFile: VirtualFile?, project: Project) {
        val includeRoot = mainFile?.parent
        val outPath = resolve()?.path ?: return

        val files: Set<PsiFile>
        try {
            files = mainFile?.psiFile(project)?.referencedFileSet() ?: emptySet()
        }
        catch (e: IndexNotReadyException) {
            throw ExecutionException("Please wait until the indices are built.", e)
        }

        // Create output paths (see issue #70 on GitHub)
        files.asSequence()
            // Ignore all output directories to avoid exponential recursion
            .filter { !it.virtualFile.path.contains(outPath) }
            .mapNotNull { FileUtil.pathRelativeTo(includeRoot?.path ?: return@mapNotNull null, it.virtualFile.parent.path) }
            .forEach { File(outPath, it).mkdirs() }
    }

    class Converter : com.intellij.util.xmlb.Converter<LatexRunConfigurationAbstractOutputPathOption>() {

        override fun toString(value: LatexRunConfigurationAbstractOutputPathOption): String {
            return LatexPathConverterUtil.toString(value)
        }

        override fun fromString(value: String): LatexRunConfigurationAbstractOutputPathOption {
            val (resolvedPath, pathWithMacro) = LatexPathConverterUtil.fromString(value)
            return LatexRunConfigurationOutputPathOption(resolvedPath, pathWithMacro)
        }
    }
}