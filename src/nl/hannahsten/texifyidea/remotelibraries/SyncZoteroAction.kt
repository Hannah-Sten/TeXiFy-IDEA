package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ApplicationManager.getApplication().invokeLater {
            CoroutineScope(Dispatchers.Default).launch {
                val credentials = PasswordSafe.instance.get(ZoteroLibrary.credentialAttributes)
                credentials?.userName?.let {
                    credentials.password?.let { apiKey ->
                        val zotero = ZoteroLibrary(it, apiKey.toString())
                        val bibItems = zotero.getCollection(project)
                        RemoteLibraryManager.getInstance().updateLibrary(zotero, bibItems)
                    }
                }
            }
        }
    }
}