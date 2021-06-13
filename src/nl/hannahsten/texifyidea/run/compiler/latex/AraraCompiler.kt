package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil

object AraraCompiler : SupportedLatexCompiler("Arara", "arara") {

    override val outputFormats = arrayOf(OutputFormat.PDF)

    override fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String?,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String> {
        // Everything handled by arara, except possibly the path to the arara executable
        return mutableListOf(
            LatexSdkUtil.getExecutableName(
                executableName,
                runConfig.project
            )
        )
    }
}