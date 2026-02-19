package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.pdfviewer.PdfViewer
import java.nio.file.Path

interface LatexCompilationRunConfiguration : RunConfiguration {

    var mainFile: VirtualFile?
    var compilerArguments: String?
    var compilerPath: String?
    var pdfViewer: PdfViewer?
    var environmentVariables: EnvironmentVariablesData
    var isAutoCompiling: Boolean

    fun getOutputFilePath(): String
    fun getOutputDirectory(): VirtualFile?
    fun getAuxilDirectory(): VirtualFile?

    fun getResolvedWorkingDirectory(): Path?
    fun hasDefaultWorkingDirectory(): Boolean
}
