package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.execution.ExecutionManager
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * State of autocompilation.
 *
 * Both the run configuration and the typedhandler should be able to access the same state, so we can run the correct run config for the correct file.
 */
object AutoCompileState {

    /** Whether an autocompile is in progress. */
    private var isCompiling = false

    /** Whether the document has changed since the last triggered autocompile. */
    private var hasChanged = false

    /** Needed to get the selected run config. */
    private var project: Project? = null

    /**
     * Tell the state the document has been changed by the user.
     */
    @Synchronized
    fun documentChanged(project: Project) {
        this.project = project

        // Remember that the document changed, so a compilation should be scheduled later
        if (isCompiling) {
            hasChanged = true
        }
        else {
            scheduleCompilation()
        }
    }

    /**
     * Tell the state a compilation has just finished.
     */
    @Synchronized
    fun compileDone() {

        // Only compile again if needed
        if (hasChanged) {
            scheduleCompilation()
        }
    }

    private fun scheduleCompilation() {

        if (project == null) {
            Notification("AutoCompileState", "Could not auto-compile", "Please make sure you have compiled the document first.", NotificationType.WARNING).notify(null)

            return
        }

        isCompiling = true
        hasChanged = false

        GlobalScope.launch {
            // Get run configuration selected in the combobox and run that one
            val runConfigSettings = RunManager.getInstance(project!!).selectedConfiguration ?: return@launch

            try {
                ExecutionManager.getInstance(project!!).restartRunProfile(project!!,
                        DefaultRunExecutor.getRunExecutorInstance(),
                        ExecutionTargetManager.getInstance(project!!).activeTarget,
                        runConfigSettings,
                        null)
            }
            finally {
                isCompiling = false
            }
        }
    }
}