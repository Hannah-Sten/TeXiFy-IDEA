package nl.rubensten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.run.BibtexRunConfiguration

/**
 * @author Thomas Schouten
 */
internal object BiberCompiler : Compiler<BibtexRunConfiguration> {

    override val displayName = "Biber"
    override val executableName = "biber"

    override fun getCommand(runConfig: BibtexRunConfiguration, project: Project): List<String>? {
        val command = mutableListOf<String>()

        command.apply {
            if (runConfig.compilerPath != null) {
                add(runConfig.compilerPath!!)
            }
            else add(executableName)

            // Biber needs auxiliary files, but the flag is different from bibtex
            add("--output_directory ${runConfig.auxDir}")

            runConfig.compilerArguments?.let { addAll(it.split("""\s+""".toRegex())) }

            add(runConfig.mainFile?.nameWithoutExtension ?: return null)
        }

        return command.toList()
    }
}