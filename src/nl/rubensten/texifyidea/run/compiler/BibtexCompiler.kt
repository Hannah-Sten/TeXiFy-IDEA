package nl.rubensten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.SystemInfo
import nl.rubensten.texifyidea.run.BibtexRunConfiguration

/**
 * @author Sten Wessel
 */
internal object BibtexCompiler : Compiler<BibtexRunConfiguration> {

    override val displayName = "BibTeX"
    override val executableName = "bibtex"

    override fun getCommand(runConfig: BibtexRunConfiguration, project: Project): List<String>? {
        val command = mutableListOf<String>()

        val moduleRoots = ProjectRootManager.getInstance(project).contentSourceRoots

        command.apply {
            add(runConfig.compilerPath ?: executableName)

            runConfig.compilerArguments?.let { addAll(it.split("""\s+""".toRegex())) }

            // Include files from auxiliary directory on Windows
            if (SystemInfo.isWindows) {
                add("-include-directory=${runConfig.mainFile?.parent?.path ?: ""}")
                addAll(moduleRoots.map { "-include-directory=${it.path}" })
            }

            add(runConfig.mainFile?.nameWithoutExtension ?: return null)
        }

        return command.toList()
    }
}
