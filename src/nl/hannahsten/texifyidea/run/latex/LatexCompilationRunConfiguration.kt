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
    var outputPath: LatexOutputPath
    var auxilPath: LatexOutputPath
    var workingDirectory: String?
    var outputFormat: LatexCompiler.Format
    var latexDistribution: LatexDistributionType
    var hasBeenRun: Boolean
    var requireFocus: Boolean
    var isAutoCompiling: Boolean

    fun getOutputFilePath(): String

    val compilationCapabilities: LatexCompilationCapabilities

    fun getResolvedWorkingDirectory(): String?

    fun hasDefaultWorkingDirectory(): Boolean

    fun setMainFile(mainFilePath: String)

    fun setFileOutputPath(fileOutputPath: String)

    fun setFileAuxilPath(fileAuxilPath: String)

    fun setSuggestedName()

    fun getLatexSdk(): Sdk?

    fun getLatexDistributionType(): LatexDistributionType

    fun getAuxilDirectory(): VirtualFile?

    fun usesAuxilOrOutDirectory(): Boolean
}
