package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import java.util.*

object LatexmkCompiler : SupportedLatexCompiler("Latexmk", "latexmk") {

    override val includesBibtex = true

    override val includesMakeindex = true

    override val handlesNumberOfCompiles = true

    override val outputFormats = arrayOf(OutputFormat.DEFAULT, OutputFormat.PDF, OutputFormat.DVI)

    override fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String?,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String> {
        val command = mutableListOf(
            LatexSdkUtil.getExecutableName(
                executableName, runConfig.project,
                runConfig.options.getLatexDistribution(runConfig.project)
            )
        )

        val isLatexmkRcFilePresent = LatexmkRcFileFinder.isLatexmkRcFilePresent(runConfig)

        // If it is present, assume that it will handle everything (command line options would overwrite latexmkrc options)
        if (!isLatexmkRcFilePresent) {
            // Adding the -pdf flag makes latexmk run with pdflatex, which is definitely preferred over running with just latex
            command.add("-pdf")
            command.add("-file-line-error")
            command.add("-interaction=nonstopmode")
            command.add("-synctex=1")
        }

        if (runConfig.options.outputFormat != OutputFormat.DEFAULT) {
            command.add("-output-format=${runConfig.options.outputFormat.name.lowercase(Locale.getDefault())}")
        }

        command.add("-output-directory=$outputPath")

        if (auxilPath != null && runConfig.options.getLatexDistribution(runConfig.project).isMiktex(runConfig.project)) {
            command.add("-aux-directory=$auxilPath")
        }

        // -include-directory does not work with latexmk
        return command
    }
}