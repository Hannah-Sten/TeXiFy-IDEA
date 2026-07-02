package nl.hannahsten.texifyidea.run.latex.flow

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.editor.autocompile.AutoCompileDoneListener
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexSessionInitializer
import nl.hannahsten.texifyidea.run.latex.LatexStepRunConfigurationOptions
import nl.hannahsten.texifyidea.run.latex.TextEditorContextSnapshot
import nl.hannahsten.texifyidea.run.latex.TextEditorSnapshot
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepContext
import nl.hannahsten.texifyidea.run.latex.step.LatexRunStepPlanBuilder
import nl.hannahsten.texifyidea.run.latex.steplog.LatexStepLogTabComponent
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.focusedTextEditor
import nl.hannahsten.texifyidea.util.selectedTextEditor

/**
 * Run-profile state that executes the step-based LaTeX pipeline.
 * It converts configured step options into a runtime plan and returns the process handler/console pair.
 */
internal class LatexStepRunState(
    private val runConfig: LatexRunConfiguration,
    private val environment: ExecutionEnvironment,
    private val configuredSteps: List<LatexStepRunConfigurationOptions>,
) : com.intellij.execution.configurations.RunProfileState {

    @Throws(ExecutionException::class)
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult {
        FileDocumentManager.getInstance().saveAllDocuments()
        val session = LatexSessionInitializer.initialize(runConfig, environment)
        // We need to capture the focused editor before execution, otherwise focus will change to the run tool window
        session.editorContext = captureEditorContext(environment.project)

        val configuredPlan = LatexRunStepPlanBuilder.build(configuredSteps)
        if (configuredPlan.unsupportedTypes.isNotEmpty()) {
            Log.warn("Unsupported compile-step types: ${configuredPlan.unsupportedTypes.joinToString(", ")}")
        }

        if (configuredPlan.steps.isEmpty()) {
            throw ExecutionException(TexifyBundle.message("run.error.no.executable.steps.in.schema"))
        }

        val context = LatexRunStepContext(runConfig, environment, session)
        val overallHandler = StepAwareSequentialProcessHandler(configuredPlan.steps, context)
        if (runConfig.isAutoCompiling) {
            overallHandler.addProcessListener(AutoCompileDoneListener(runConfig))
        }
        val stepLogConsole = LatexStepLogTabComponent(environment.project, session.mainFile, overallHandler)

        return DefaultExecutionResult(stepLogConsole, overallHandler)
    }

    private fun captureEditorContext(project: Project): TextEditorContextSnapshot? {
        val app = ApplicationManager.getApplication()
        val context = if (app.isDispatchThread) {
            readEditorContext(project)
        }
        else {
            var captured: TextEditorContextSnapshot? = null
            app.invokeAndWait {
                captured = readEditorContext(project)
            }
            captured
        }
        return context?.takeIf { it.focused != null || it.selected != null }
    }

    private fun readEditorContext(project: Project): TextEditorContextSnapshot =
        TextEditorContextSnapshot(
            focused = snapshot(project.focusedTextEditor()),
            selected = snapshot(project.selectedTextEditor()),
        )

    private fun snapshot(editor: TextEditor?): TextEditorSnapshot? {
        val textEditor = editor ?: return null
        val sourceFile = FileDocumentManager.getInstance().getFile(textEditor.editor.document) ?: return null
        val line = textEditor.editor.document.getLineNumber(textEditor.editor.caretModel.offset) + 1
        return TextEditorSnapshot(
            sourceFilePath = sourceFile.path,
            line = line,
        )
    }
}
