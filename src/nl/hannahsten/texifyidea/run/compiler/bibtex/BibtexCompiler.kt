package nl.hannahsten.texifyidea.run.compiler.bibtex

import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.TeXception
import nl.hannahsten.texifyidea.run.step.BibliographyCompileStep
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * @author Sten Wessel
 */
object BibtexCompiler : SupportedBibliographyCompiler("BibTeX", "bibtex") {

    override fun getCommand(step: BibliographyCompileStep): List<String> {
        val command = mutableListOf<String>()

        val moduleRoots = ProjectRootManager.getInstance(step.configuration.project).contentSourceRoots

        command.apply {
            add(LatexSdkUtil.getExecutableName(executableName, step.configuration.project))

            step.state.compilerArguments?.let { addAll(ParametersListUtil.parse(it)) }

            // Include files from auxiliary directory on MiKTeX
            if (step.configuration.options.latexDistribution.isMiktex()) {
                add("-include-directory=${step.configuration.options.mainFile.resolve()?.parent?.path ?: ""}")
                addAll(moduleRoots.map { "-include-directory=${it.path}" })
            }

            add(step.state.mainFileName ?: throw TeXception("Unknown main file"))
        }

        return command.toList()
    }
}
