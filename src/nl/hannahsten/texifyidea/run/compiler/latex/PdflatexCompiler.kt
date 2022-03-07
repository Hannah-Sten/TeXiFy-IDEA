package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.util.*

object PdflatexCompiler : SupportedLatexCompiler("pdfLaTeX", "pdflatex") {

    override fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String?,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String> {
        // For now only support custom executable for TeX Live
        // At least avoids prepending a full path to a supposed TeX Live executable when in fact it will be prepended by a docker command
        val executable = LatexSdkUtil.getExecutableName(executableName, runConfig.project, runConfig.options.getLatexDistribution(runConfig.project))
        val command = mutableListOf(executable)

        command.add("-file-line-error")
        command.add("-interaction=nonstopmode")
        command.add("-synctex=1")
        command.add("-output-format=${runConfig.options.outputFormat.name.lowercase(Locale.getDefault())}")

        command.add("-output-directory=$outputPath")

        // -aux-directory only exists on MiKTeX
        if (auxilPath != null && runConfig.options.getLatexDistribution(runConfig.project).isMiktex()) {
            command.add("-aux-directory=$auxilPath")
        }

        // Prepend root paths to the input search path
        if (runConfig.options.getLatexDistribution(runConfig.project).isMiktex()) {
            moduleRoots.forEach {
                command.add("-include-directory=${it.path}")
            }
        }

        return command
    }
}