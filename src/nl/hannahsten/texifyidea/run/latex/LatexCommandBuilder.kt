package nl.hannahsten.texifyidea.run.latex

import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCommandBuilder
import nl.hannahsten.texifyidea.run.latexmk.LatexmkRunConfiguration

object LatexCommandBuilder {

    fun build(runConfig: LatexCompilationRunConfiguration, project: Project): List<String>? = when (runConfig) {
        is LatexRunConfiguration -> runConfig.compiler?.getCommand(runConfig, project)
        is LatexmkRunConfiguration -> LatexmkCommandBuilder.buildCommand(runConfig, project)
        else -> null
    }
}
