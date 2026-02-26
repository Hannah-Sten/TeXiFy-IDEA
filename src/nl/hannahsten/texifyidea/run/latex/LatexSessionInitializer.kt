package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.SmartPointerManager
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler.Format
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCompileMode
import nl.hannahsten.texifyidea.run.latexmk.compileModeFromMagicCommand
import java.nio.file.Path
import java.util.Locale

internal object LatexSessionInitializer {

    @Throws(ExecutionException::class)
    fun initialize(
        runConfig: LatexRunConfiguration,
        environment: ExecutionEnvironment,
        executionState: LatexRunSessionState,
    ) {
        if (executionState.isInitialized) {
            return
        }

        val mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig)
            ?: throw ExecutionException("Main file cannot be resolved")
        executionState.resolvedMainFile = mainFile
        executionState.psiFile = ReadAction.compute<com.intellij.psi.SmartPsiElementPointer<com.intellij.psi.PsiFile>?, RuntimeException> {
            val mainPsiFile = PsiManager.getInstance(runConfig.project).findFile(mainFile) ?: return@compute null
            SmartPointerManager.getInstance(runConfig.project).createSmartPsiElementPointer(mainPsiFile)
        }
        executionState.resolvedWorkingDirectory = LatexPathResolver.resolve(runConfig.workingDirectory, mainFile, environment.project)
            ?: Path.of(mainFile.parent.path)

        executionState.resolvedOutputDir = LatexPathResolver.resolveOutputDir(runConfig, mainFile)
            ?: throw ExecutionException("Output directory cannot be resolved")
        executionState.resolvedAuxDir = LatexPathResolver.resolveAuxDir(runConfig, mainFile)

        if (!runConfig.getLatexDistributionType().isMiktex(runConfig.project, mainFile)) {
            val createdDirectories = LatexPathResolver.updateOutputSubDirs(runConfig, mainFile, executionState.resolvedOutputDir)
            executionState.addCleanupDirectoriesIfEmpty(createdDirectories)
        }

        refreshCompileStepDerivedState(runConfig, executionState, runConfig.primaryCompileStep())
        executionState.isInitialized = true
    }

    fun refreshCompileStepDerivedState(
        runConfig: LatexRunConfiguration,
        executionState: LatexRunSessionState,
        step: LatexStepRunConfigurationOptions?,
    ) {
        val mainFile = executionState.resolvedMainFile ?: return
        val latexmkStep = step as? LatexmkCompileStepOptions
        val classicStep = step as? LatexCompileStepOptions

        val effectiveMode = latexmkStep?.let { LatexmkModeService.effectiveCompileMode(runConfig, executionState, it) }
        executionState.effectiveLatexmkCompileMode = effectiveMode
        executionState.effectiveCompilerArguments = when {
            latexmkStep != null -> LatexmkModeService.buildArguments(runConfig, executionState, latexmkStep, effectiveMode)
            classicStep != null -> classicStep.compilerArguments
            else -> null
        }
        executionState.resolvedOutputFilePath = computeOutputFilePath(executionState, mainFile, step)
    }

    private fun computeOutputFilePath(
        executionState: LatexRunSessionState,
        mainFile: VirtualFile,
        step: LatexStepRunConfigurationOptions?,
    ): String? {
        val outputDirPath = executionState.resolvedOutputDir?.path ?: return null
        val baseName = mainFile.nameWithoutExtension
        val extension = if (step is LatexmkCompileStepOptions) {
            val modeFromArgs = executionState.effectiveCompilerArguments
                ?.takeIf(String::isNotBlank)
                ?.let { compileModeFromMagicCommand("latexmk $it") }
            (modeFromArgs ?: executionState.effectiveLatexmkCompileMode ?: LatexmkCompileMode.PDFLATEX_PDF)
                .extension
                .lowercase(Locale.getDefault())
        }
        else if ((step as? LatexCompileStepOptions)?.outputFormat == Format.DEFAULT) {
            "pdf"
        }
        else {
            ((step as? LatexCompileStepOptions)?.outputFormat ?: Format.PDF).toString().lowercase(Locale.getDefault())
        }
        return "$outputDirPath/$baseName.$extension"
    }
}
