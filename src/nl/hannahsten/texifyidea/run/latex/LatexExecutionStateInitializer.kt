package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.latexmk.compileModeFromMagicCommand
import java.nio.file.Path
import java.util.Locale

internal object LatexExecutionStateInitializer {

    @Throws(ExecutionException::class)
    fun initialize(
        runConfig: LatexRunConfiguration,
        environment: ExecutionEnvironment,
        executionState: LatexRunExecutionState,
    ) {
        if (executionState.isInitialized) {
            return
        }

        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
            ?: throw ExecutionException("Main file cannot be resolved")
        executionState.resolvedMainFile = mainFile
        executionState.resolvedWorkingDirectory = LatexPathResolver.resolve(runConfig.workingDirectory, mainFile, environment.project)
            ?: Path.of(mainFile.parent.path)

        executionState.resolvedOutputDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile)
            ?: throw ExecutionException("Output directory cannot be resolved")
        executionState.resolvedAuxDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile)

        if (!runConfig.getLatexDistributionType().isMiktex(runConfig.project, mainFile)) {
            val createdDirectories = LatexPathResolver.updateOutputSubDirs(runConfig, mainFile, executionState.resolvedOutputDir)
            executionState.addCleanupDirectoriesIfEmpty(createdDirectories)
        }

        val effectiveMode = if (runConfig.compiler == LatexCompiler.LATEXMK) {
            LatexmkModeService.effectiveCompileMode(runConfig)
        }
        else {
            null
        }
        executionState.effectiveLatexmkCompileMode = effectiveMode
        executionState.effectiveCompilerArguments = if (runConfig.compiler == LatexCompiler.LATEXMK) {
            LatexmkModeService.buildArguments(runConfig, effectiveMode)
        }
        else {
            runConfig.compilerArguments
        }
        executionState.resolvedOutputFilePath = computeOutputFilePath(runConfig, executionState, mainFile)
        executionState.isInitialized = true
    }

    private fun computeOutputFilePath(
        runConfig: LatexRunConfiguration,
        executionState: LatexRunExecutionState,
        mainFile: VirtualFile,
    ): String? {
        val outputDirPath = executionState.resolvedOutputDir?.path ?: return null
        val baseName = mainFile.nameWithoutExtension
        val extension = if (runConfig.compiler == LatexCompiler.LATEXMK) {
            val modeFromArgs = executionState.effectiveCompilerArguments
                ?.takeIf(String::isNotBlank)
                ?.let { compileModeFromMagicCommand("latexmk $it") }
            (modeFromArgs ?: executionState.effectiveLatexmkCompileMode ?: LatexmkCompileMode.PDFLATEX_PDF)
                .extension
                .lowercase(Locale.getDefault())
        }
        else if (runConfig.outputFormat == Format.DEFAULT) {
            "pdf"
        }
        else {
            runConfig.outputFormat.toString().lowercase(Locale.getDefault())
        }
        return "$outputDirPath/$baseName.$extension"
    }
}
