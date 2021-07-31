package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.compiler.CustomCompiler
import nl.hannahsten.texifyidea.run.step.LatexCompileStep

class CustomLatexCompiler(override val executablePath: String) : LatexCompiler(),
    CustomCompiler<LatexCompileStep> {

    override fun getCommand(step: LatexCompileStep): List<String> {
        val runConfig = step.configuration
        val command = mutableListOf(executablePath)

        // Custom compiler arguments specified by the user
        runConfig.options.compilerArguments?.let { arguments ->
            ParametersListUtil.parse(arguments)
                .forEach { command.add(it) }
        }

        val mainFile = runConfig.options.mainFile.resolve() ?: return mutableListOf()
        command.add(mainFile.name)

        return command
    }
}