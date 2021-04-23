package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

object XelatexCompiler : SupportedLatexCompiler("XeLaTeX", "xelatex") {

    override val supportsUnicode = true

    override val outputFormats = arrayOf(OutputFormat.PDF, OutputFormat.XDV)

        override fun createCommand(
            runConfig: LatexRunConfiguration,
            auxilPath: String?,
            outputPath: String?,
            moduleRoot: VirtualFile?,
            moduleRoots: Array<VirtualFile>
        ): MutableList<String> {
            val command = mutableListOf(runConfig.compilerPath ?: LatexSdkUtil.getExecutableName(
                executableName,
                runConfig.project
            )
            )

        // As usual, available command line options can be viewed with xelatex --help
        // On TeX Live, installing collection-xetex should be sufficient to get xelatex
        command.add("-file-line-error")
        command.add("-interaction=nonstopmode")
        command.add("-synctex=1")

        if (runConfig.outputFormat == OutputFormat.XDV) {
            command.add("-no-pdf")
        }

        command.add("-output-directory=$outputPath")

        if (auxilPath != null && runConfig.getLatexDistributionType().isMiktex()) {
            command.add("-aux-directory=$auxilPath")
        }

        // Prepend root paths to the input search path
        if (runConfig.getLatexDistributionType().isMiktex()) {
            moduleRoots.forEach {
                command.add("-include-directory=${it.path}")
            }
        }

        return command
    }
}