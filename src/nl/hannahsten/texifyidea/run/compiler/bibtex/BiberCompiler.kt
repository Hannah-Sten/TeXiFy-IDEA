package nl.hannahsten.texifyidea.run.compiler.bibtex

import com.intellij.util.execution.ParametersListUtil
import nl.hannahsten.texifyidea.run.step.BibliographyCompileStep
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

/**
 * @author Thomas Schouten
 */
object BiberCompiler : SupportedBibliographyCompiler("Biber", "biber") {

    override fun getCommand(step: BibliographyCompileStep): List<String>? = mutableListOf<String>().apply {
        add(LatexSdkUtil.getExecutableName(executableName, step.configuration.project))

        // Biber can find auxiliary files, but the flag is different from bibtex.
        // The following flag assumes the command is executed in the directory where the .bcf control file is.
        // The extra directory added is the directory from which the path to the .bib resource file is searched as specified in the .bcf file.
        add("--input-directory=${step.configuration.options.mainFile.resolve()?.parent?.path ?: ""}")

        step.state.compilerArguments?.let { addAll(ParametersListUtil.parse(it)) }

        add(step.configuration.options.mainFile.resolve()?.nameWithoutExtension ?: return null)
    }
}