package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Provides a service for running coroutines in the Texify plugin.
 * We should not create [CoroutineScope] manually, but let the platform create it for us.
 *
 * See [Launching Coroutine From Service Scope](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#launching-coroutine-from-service-scope).
 */
@Service
class TexifyCoroutine(val coroutineScope: CoroutineScope) {
    companion object {
        fun getInstance(): TexifyCoroutine {
            return ApplicationManager.getApplication().getService(TexifyCoroutine::class.java)
        }

        /**
         * Run a coroutine in the background using the Texify plugin's coroutine scope.
         */
        fun runInBackground(action: suspend CoroutineScope.() -> Unit) {
            getInstance().coroutineScope.launch {
                action()
            }
        }

        fun runInBackgroundReadAction(action: () -> Unit) {
            getInstance().coroutineScope.launch {
                readAction {
                    action()
                }
            }
        }
    }
}