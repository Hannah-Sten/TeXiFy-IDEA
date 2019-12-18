package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.execution.impl.RunConfigurationBeforeRunProvider
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.runners.ExecutionEnvironment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.hannahsten.texifyidea.run.latex.LatexRunConfiguration

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

    /** State needed to schedule a compilation. */
    private var environment: ExecutionEnvironment? = null

    /** The last run runconfig. */
    private var runConfig: LatexRunConfiguration? = null

    /**
     * Tell the state the document has been changed by the user.
     */
    @Synchronized
    fun documentChanged() {
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
    fun compileDone(environment: ExecutionEnvironment, runConfig: LatexRunConfiguration) {

        this.environment = environment
        this.runConfig = runConfig

        // Only compile again if needed
        if (hasChanged) {
            hasChanged = false
            scheduleCompilation()
        }
        else {
            isCompiling = false
        }
    }

    private fun scheduleCompilation() {

        if (environment == null || runConfig == null) {
            // Fail silently? todo show unobtrusive warning somewhere
            return
        }

        isCompiling = true

        GlobalScope.launch {
            val latexSettings = RunManagerImpl.getInstanceImpl(environment!!.project).getSettings(runConfig!!)
            if (latexSettings != null) {
                RunConfigurationBeforeRunProvider.doExecuteTask(environment!!, latexSettings, null)
            }
        }
    }
}