package nl.hannahsten.texifyidea.run.latex

import com.intellij.execution.ExecutionException
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiManager
import com.intellij.psi.SmartPointerManager
import nl.hannahsten.texifyidea.run.compiler.LatexCompiler
import java.nio.file.Path
import java.util.Locale

internal object LatexSessionInitializer {

    @Throws(ExecutionException::class)
    fun initialize(
        runConfig: LatexRunConfiguration,
        environment: ExecutionEnvironment,
    ): LatexRunSessionState = initializeInternal(
        runConfig = runConfig,
        mainFile = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig),
        workingDirectoryProject = environment.project,
        createOutputSubDirs = true,
    )

    @Throws(ExecutionException::class)
    fun initializeForModel(
        runConfig: LatexRunConfiguration,
        mainFile: com.intellij.openapi.vfs.VirtualFile? = LatexRunConfigurationStaticSupport.resolveMainFile(runConfig),
    ): LatexRunSessionState = initializeInternal(
        runConfig = runConfig,
        mainFile = mainFile,
        workingDirectoryProject = runConfig.project,
        createOutputSubDirs = false,
    )

    @Throws(ExecutionException::class)
    private fun initializeInternal(
        runConfig: LatexRunConfiguration,
        mainFile: com.intellij.openapi.vfs.VirtualFile?,
        workingDirectoryProject: com.intellij.openapi.project.Project,
        createOutputSubDirs: Boolean,
    ): LatexRunSessionState {
        val resolvedMainFile = mainFile ?: throw ExecutionException("Main file cannot be resolved")

        val outputDir = LatexPathResolver.resolveOutputDir(runConfig, resolvedMainFile)
            ?: throw ExecutionException("Output directory cannot be resolved")
        val auxDir = LatexPathResolver.resolveAuxDir(runConfig, resolvedMainFile)
        val workingDirectory = LatexPathResolver.resolve(runConfig.workingDirectory, resolvedMainFile, workingDirectoryProject)
            ?: Path.of(resolvedMainFile.parent.path)

        val psiPointer = ReadAction.compute<com.intellij.psi.SmartPsiElementPointer<com.intellij.psi.PsiFile>?, RuntimeException> {
            val mainPsiFile = PsiManager.getInstance(runConfig.project).findFile(resolvedMainFile) ?: return@compute null
            SmartPointerManager.getInstance(runConfig.project).createSmartPsiElementPointer(mainPsiFile)
        }

        val distributionType = runConfig.getLatexDistributionType()
        val session = LatexRunSessionState(
            project = runConfig.project,
            mainFile = resolvedMainFile,
            outputDir = outputDir,
            workingDirectory = workingDirectory,
            distributionType = distributionType,
            usesDefaultWorkingDirectory = runConfig.hasDefaultWorkingDirectory(),
            latexSdk = runConfig.getLatexSdk(),
            auxDir = auxDir,
            psiFile = psiPointer,
        )

        if (createOutputSubDirs && !distributionType.isMiktex(runConfig.project, resolvedMainFile)) {
            val createdDirectories = LatexPathResolver.updateOutputSubDirs(runConfig, resolvedMainFile, outputDir)
            session.addCleanupDirectoriesIfEmpty(createdDirectories)
        }

        updateOutputFilePath(session, runConfig.primaryCompileStep())
        return session
    }

    fun updateOutputFilePath(
        session: LatexRunSessionState,
        step: LatexStepRunConfigurationOptions?,
    ) {
        val baseName = session.mainFile.nameWithoutExtension
        val extension = when (step) {
            is LatexCompileStepOptions -> {
                if (step.outputFormat == LatexCompiler.Format.DEFAULT) {
                    "pdf"
                }
                else {
                    step.outputFormat.toString().lowercase(Locale.getDefault())
                }
            }
            is LatexmkCompileStepOptions -> "pdf"
            else -> "pdf"
        }
        session.resolvedOutputFilePath = "${session.outputDir.path}/$baseName.$extension"
    }
}
