package nl.rubensten.texifyidea.run.compiler

import com.intellij.openapi.project.Project
import nl.rubensten.texifyidea.run.BibtexRunConfiguration

/**
 *
 * @author Sten Wessel
 */
internal object BibtexCompiler : Compiler<BibtexRunConfiguration> {

    override val displayName = "BibTeX"

    override fun getCommand(runConfig: BibtexRunConfiguration, project: Project): List<String>? {
        val command = mutableListOf<String>()


        return command.toList()
    }
}
