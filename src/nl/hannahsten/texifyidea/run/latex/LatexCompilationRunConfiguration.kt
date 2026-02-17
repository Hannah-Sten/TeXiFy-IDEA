package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer

/**
 * Contract shared by LaTeX-like run configurations.
 */
interface LatexCompilationRunConfiguration : RunConfiguration {

    var compiler: LatexCompiler?
    var compilerPath: String?
    var pdfViewer: PdfViewer?
    var viewerCommand: String?
    var compilerArguments: String?
    var expandMacrosEnvVariables: Boolean
    var environmentVariables: EnvironmentVariablesData
    var beforeRunCommand: String?
    var mainFile: VirtualFile?

    /** Final output directory (e.g. pdf); maps to outdir/output-directory flags. */
    var outputPath: LatexOutputPath

    /** Auxiliary/intermediate files directory (e.g. aux/log/toc); may be separate from outputPath. */
    var auxilPath: LatexOutputPath

    /** Process working directory (cwd) used to resolve relative paths; independent from output/aux directories. */
    var workingDirectory: String?
    var outputFormat: LatexCompiler.Format
    var latexDistribution: LatexDistributionType
    var hasBeenRun: Boolean
    var requireFocus: Boolean
    var isAutoCompiling: Boolean

    fun getOutputFilePath(): String

    val compilationCapabilities: LatexCompilationCapabilities

    fun getResolvedWorkingDirectory(): String? =
        if (!workingDirectory.isNullOrBlank() && mainFile != null) {
            workingDirectory?.replace(LatexOutputPath.MAIN_FILE_STRING, mainFile!!.parent.path)
        }
        else {
            mainFile?.parent?.path
        }

    fun hasDefaultWorkingDirectory(): Boolean = workingDirectory == LatexOutputPath.MAIN_FILE_STRING

    fun setMainFile(mainFilePath: String)

    fun setFileOutputPath(fileOutputPath: String)

    fun setFileAuxilPath(fileAuxilPath: String)

    fun setSuggestedName()

    fun getLatexSdk(): Sdk?

    fun getLatexDistributionType(): LatexDistributionType

    fun getAuxilDirectory(): VirtualFile?

    fun usesAuxilOrOutDirectory(): Boolean
}
