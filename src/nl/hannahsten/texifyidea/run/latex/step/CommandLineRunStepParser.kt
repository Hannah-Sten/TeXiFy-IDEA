package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.util.execution.ParametersListUtil

internal object CommandLineRunStepParser {

    fun parse(commandLine: String): List<String> = ParametersListUtil.parse(commandLine.trim())
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
