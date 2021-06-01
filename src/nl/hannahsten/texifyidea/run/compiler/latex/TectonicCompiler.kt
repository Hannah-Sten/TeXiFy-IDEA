package nl.hannahsten.texifyidea.run.compiler.latex

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.sdk.LatexSdkUtil


object TectonicCompiler : SupportedLatexCompiler("Tectonic", "tectonic") {

    override val includesBibtex = true

    override val handlesNumberOfCompiles = true

    override val outputFormats = arrayOf(OutputFormat.PDF, OutputFormat.HTML, OutputFormat.XDV, OutputFormat.AUX)

    override fun createCommand(
        runConfig: LatexRunConfiguration,
        auxilPath: String?,
        outputPath: String?,
        moduleRoot: VirtualFile?,
        moduleRoots: Array<VirtualFile>
    ): MutableList<String> {

            // The available command line arguments can be found at https://github.com/tectonic-typesetting/tectonic/blob/d7a8497c90deb08b5e5792a11d6e8b082f53bbb7/src/bin/tectonic.rs#L158
            val command = mutableListOf(LatexSdkUtil.getExecutableName(executableName, runConfig.project))

        command.add("--synctex")

        command.add("--outfmt=${runConfig.options.outputFormat.name.toLowerCase()}")

        if (outputPath != null) {
            command.add("--outdir=$outputPath")
        }

        return command
    }
}
