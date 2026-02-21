package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import java.nio.file.Path

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

        val mainFile = runConfig.resolveMainFile()
            ?: throw ExecutionException("Main file cannot be resolved")
        executionState.resolvedMainFile = mainFile
        executionState.resolvedWorkingDirectory = LatexPathResolver.resolve(runConfig.workingDirectory, mainFile, environment.project)
            ?: Path.of(mainFile.parent.path)

        executionState.resolvedOutputDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile)
            ?: throw ExecutionException("Output directory cannot be resolved")
        executionState.resolvedAuxDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile)

        if (!runConfig.getLatexDistributionType().isMiktex(runConfig.project, mainFile)) {
            val createdDirectories = LatexPathResolver.updateOutputSubDirs(runConfig, mainFile, executionState.resolvedOutputDir)
            runConfig.filesToCleanUpIfEmpty.addAll(createdDirectories)
        }

        val effectiveMode = if (runConfig.compiler == LatexCompiler.LATEXMK) {
            runConfig.effectiveLatexmkCompileMode()
        }
        else {
            null
        }
        executionState.effectiveLatexmkCompileMode = effectiveMode
        executionState.effectiveCompilerArguments = if (runConfig.compiler == LatexCompiler.LATEXMK) {
            runConfig.buildLatexmkArguments()
        }
        else {
            runConfig.compilerArguments
        }
        executionState.initFingerprint = buildFingerprint(runConfig, mainFile)
        executionState.isInitialized = true
    }

    private fun buildFingerprint(runConfig: LatexRunConfiguration, mainFile: VirtualFile): String = listOf(
        mainFile.path,
        runConfig.compiler?.name,
        runConfig.compilerPath,
        runConfig.mainFilePath,
        runConfig.outputPath?.toString(),
        runConfig.auxilPath?.toString(),
        runConfig.workingDirectory?.toString(),
        runConfig.latexmkCompileMode.name,
        runConfig.latexmkCustomEngineCommand,
        runConfig.latexmkCitationTool.name,
        runConfig.latexmkExtraArguments,
        runConfig.compilerArguments,
    ).joinToString("|")
}
