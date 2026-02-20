package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

object TexliveonflyCompiler : SupportedLatexCompiler("Texliveonfly", "texliveonfly") {

    override val outputFormats = arrayOf(OutputFormat.PDF)

    override fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String?,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String> {
        val command = mutableListOf(
            LatexSdkUtil.getExecutableName(
                executableName,
                runConfig.project,
                latexDistributionType = runConfig.options.getLatexDistribution(runConfig.project)
            )
        )

        // texliveonfly is a Python script which calls other compilers (by default pdflatex), main feature is downloading packages automatically
        // commands can be passed to those compilers with the arguments flag, however apparently IntelliJ cannot handle quotes so we cannot pass multiple arguments to pdflatex.
        // Fortunately, -synctex=1 and -interaction=nonstopmode are on by default in texliveonfly
        // Since adding one will work without any quotes, we choose the output directory.
        if (outputPath != null) {
            command.add("--arguments=--output-directory=$outputPath")
        }

        return command
    }
}