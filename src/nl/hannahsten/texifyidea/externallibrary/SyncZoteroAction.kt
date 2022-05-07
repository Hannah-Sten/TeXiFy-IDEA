package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import kotlinx.coroutines.*

class SyncZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ApplicationManager.getApplication().invokeLater {
            CoroutineScope(Dispatchers.Default).launch {
                val zotero = ZoteroLibrary()
                val bibItems = zotero.getCollection(project)
//                ExternalLibraryManager.getInstance().updateLibrary(zotero, emptyList())
                ExternalLibraryManager.getInstance().updateLibrary(zotero, bibItems)
//                ApplicationManager.getApplication().getComponent(ExternalLibraryManager::class.java).updateLibrary(zotero, bibItems)
//                project.service<ExternalLibraryManager>().updateLibrary(zotero, bibItems)
            }
        }
    }
}