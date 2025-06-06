package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.execution.ExecutionManager
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.run.LatexRunConfiguration
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration
import nl.hannahsten.texifyidea.settings.TexifySettings
import java.util.concurrent.atomic.AtomicBoolean

/**
 * State of autocompilation.
 *
 * Both the run configuration and the typedhandler should be able to access the same state, so we can run the correct run config for the correct file.
 */
object AutoCompileState {

    /** Whether an autocompile is in progress. */
    private val isCompiling = AtomicBoolean(false)

    /**
     * Whether there is a (pending) request to trigger autocompile,
     * not necessarily meaning that the document has changed.
     */
    private var recentRequest = false

    /** Needed to get the selected run config. */
    private var project: Project? = null

    /**
     * Request an auto compilation of the document.
     */
    @Synchronized
    fun requestAutoCompilation(project: Project) {
        this.project = project
        recentRequest = true
        // Remember that the document changed, so a compilation should be scheduled later
        if (!isCompiling.get()) {
            scheduleCompilation()
        }
    }

    /**
     * Tell the state a compilation has just finished.
     */
    @Synchronized
    fun compilationFinished() {
        isCompiling.set(false)
        // Process any pending requests to compile
        if (recentRequest) {
            scheduleCompilation()
        }
    }

    private fun scheduleCompilation() {
        val proj = this.project
        if (proj == null) {
            Notification("LaTeX", "Could not auto-compile", "Please make sure you have compiled the document first.", NotificationType.WARNING).notify(null)
            return
        }
        if (!TexifySettings.getInstance().isAutoCompileEnabled()) {
            return
        }

        // Get run configuration selected in the combobox and run that one
        if (proj.isDisposed) return
        val runConfigSettings = RunManager.getInstance(proj).selectedConfiguration

        val runConfig = runConfigSettings?.configuration
        if (runConfig !is LatexRunConfiguration) {
            Notification("LaTeX", "Could not auto-compile", "Please make sure you have a valid LaTeX run configuration selected.", NotificationType.WARNING).notify(null)
            return
        }

        // Ensure we only trigger one compilation at a time
        if (isCompiling.getAndSet(true)) return

        recentRequest = false
        // Remember that it is auto compiling so we won't interrupt the user during typing
        runConfig.isAutoCompiling = true

        // If the run config is already running, this may trigger a dialog asking the user whether to stop and rerun
        ExecutionManager.getInstance(proj).restartRunProfile(
            proj,
            DefaultRunExecutor.getRunExecutorInstance(),
            ExecutionTargetManager.getInstance(proj).activeTarget,
            runConfigSettings,
            null
        )
    }
}