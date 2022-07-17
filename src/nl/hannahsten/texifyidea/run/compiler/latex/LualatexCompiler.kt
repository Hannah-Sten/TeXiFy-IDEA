package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil
import java.util.*

object LualatexCompiler : SupportedLatexCompiler("LuaLaTeX", "lualatex") {

    override val defaultArguments = "-interaction=nonstopmode -file-line-error -synctex=1"

    override val supportsUnicode = true

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
                    runConfig.options.getLatexDistribution(runConfig.project)
                )
            )

        // Some commands are the same as for pdflatex
        command.add("-output-format=${runConfig.options.outputFormat.name.lowercase(Locale.getDefault())}")

        command.add("-output-directory=$outputPath")

        // Note that lualatex has no -aux-directory
        return command
    }
}