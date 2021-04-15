package nl.hannahsten.texifyidea.run.compiler.bibtex

import com.intellij.openapi.roots.ProjectRootManager
import nl.hannahsten.texifyidea.run.legacy.bibtex.BibtexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * @author Sten Wessel
 */
object BibtexCompiler : SupportedBibliographyCompiler("BibTeX", "bibtex") {

    override fun createCommand(runConfig: BibtexRunConfiguration): List<String>? {
        val command = mutableListOf<String>()

        val moduleRoots = ProjectRootManager.getInstance(runConfig.project).contentSourceRoots

        command.apply {
            add(runConfig.compilerPath ?: executableName)

            runConfig.compilerArguments?.let { addAll(it.split("""\s+""".toRegex())) }

            // Include files from auxiliary directory on Windows
            if (LatexSdkUtil.isMiktexAvailable) {
                add("-include-directory=${runConfig.mainFile?.parent?.path ?: ""}")
                addAll(moduleRoots.map { "-include-directory=${it.path}" })
            }

            add(runConfig.mainFile?.nameWithoutExtension ?: return null)
        }

        return command.toList()
    }
}
