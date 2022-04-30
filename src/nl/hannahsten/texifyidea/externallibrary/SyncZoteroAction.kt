package nl.hannahsten.texifyidea.externallibrary

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class SyncZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ApplicationManager.getApplication().invokeLater {
            GlobalScope.launch {
                val zotero = ZoteroLibrary()
                val bibItems = zotero.getCollection(project)
                ApplicationManager.getApplication().getComponent(ExternalLibraryManager::class.java).updateLibrary(zotero, bibItems)
//                project.service<ExternalLibraryManager>().updateLibrary(zotero, bibItems)
            }
        }
    }
}