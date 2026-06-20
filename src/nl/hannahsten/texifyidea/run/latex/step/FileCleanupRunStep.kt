package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessHandler
import nl.hannahsten.texifyidea.run.common.createCompilationHandler
import nl.hannahsten.texifyidea.run.latex.FileCleanupSupport
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.run.latex.LatexmkCompileStepOptions
import nl.hannahsten.texifyidea.run.latexmk.LatexmkCleanUtil

internal class FileCleanupRunStep(
    stepConfig: FileCleanupStepOptions,
) : LatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    override fun beforeStart(context: LatexRunStepContext) {
        if (usesLatexmkCleanup(context)) {
            return
        }

        try {
            FileCleanupSupport.delete(FileCleanupSupport.collectRunTargets(context))
        }
        finally {
            context.session.filesToCleanUp.clear()
        }
    }

    @Throws(ExecutionException::class)
    override fun createProcess(context: LatexRunStepContext): ProcessHandler? {
        if (!usesLatexmkCleanup(context)) {
            return null
        }

        val command = LatexmkCleanUtil.buildCleanCommandForModel(
            runConfig = context.runConfig,
            mainFile = context.session.mainFile,
            cleanAll = false,
        ) ?: throw ExecutionException("Could not build latexmk cleanup command.")

        return createCompilationHandler(
            context = context,
            command = command,
            workingDirectory = context.session.workingDirectory,
        )
    }

    override fun afterFinish(context: LatexRunStepContext, exitCode: Int) {
        if (!usesLatexmkCleanup(context)) {
            return
        }

        try {
            if (exitCode == 0) {
                FileCleanupSupport.delete(context.session.filesToCleanUp.toList())
            }
        }
        finally {
            context.session.filesToCleanUp.clear()
        }
    }

    private fun usesLatexmkCleanup(context: LatexRunStepContext): Boolean =
        context.runConfig.primaryCompileStep() is LatexmkCompileStepOptions
}
