package nl.hannahsten.texifyidea.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * Provides a service for running coroutines in the Texify plugin.
 */
@Service
class TexifyCoroutine(
    val coroutineScope: CoroutineScope
) {
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
    }
}