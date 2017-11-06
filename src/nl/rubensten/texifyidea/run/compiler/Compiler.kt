package nl.rubensten.texifyidea.run.compiler

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

/**
 *
 * @author Sten Wessel
 */
interface Compiler<in R : RunConfiguration> {

    val displayName: String
    val executableName: String

    fun getCommand(runConfig: R, project: Project): List<String>?

}
