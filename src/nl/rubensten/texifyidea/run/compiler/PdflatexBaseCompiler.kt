package nl.rubensten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import nl.rubensten.texifyidea.run.LatexRunConfiguration

/**
 * @author Sten Wessel
 */
internal abstract class PdflatexBaseCompiler : Compiler<LatexRunConfiguration> {

    override val executableName = "pdflatex"

    override fun getCommand(runConfig: LatexRunConfiguration, project: Project): List<String>? {
        val command = mutableListOf<String>()

        val rootManager = ProjectRootManager.getInstance(project)
        val moduleRoot = rootManager.fileIndex.getContentRootForFile(runConfig.mainFile) ?: return null
        val moduleRoots = rootManager.contentSourceRoots

        command.apply {
            if (runConfig.compilerPath != null) {
                add(runConfig.compilerPath!!)
            } else {
                add(executableName)
            }

            add("-file-line-error")
            add("-interaction=nonstopmode")
            add("-synctex=1")
            add("-output-format=" + runConfig.outputFormat.name.toLowerCase())
            add("-output-directory=" + moduleRoot.path + "/out")

            addAll(getSpecificArguments(runConfig, project, moduleRoot, moduleRoots))

            // Custom arguments supplied by the user
            runConfig.compilerArguments?.let { addAll(it.split("""\s+""".toRegex())) }

            add(runConfig.mainFile?.nameWithoutExtension ?: return null)
        }

        return command.toList()
    }

    abstract fun getSpecificArguments(runConfig: LatexRunConfiguration, project: Project, moduleRoot: VirtualFile, moduleRoots: Array<VirtualFile>): List<String>
}
