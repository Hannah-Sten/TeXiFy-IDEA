package nl.hannahsten.texifyidea.run.pdfviewer

import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@com.intellij.openapi.components.Service
class PdfViewerService(val coroutineScope: CoroutineScope) {
    companion object {
        @JvmStatic
        fun getInstance(): PdfViewerService {
            return ApplicationManager.getApplication().getService(PdfViewerService::class.java)
        }

        fun runInBackground(action: suspend CoroutineScope.() -> Unit) {
            getInstance().coroutineScope.launch {
                action()
            }
        }
    }
}