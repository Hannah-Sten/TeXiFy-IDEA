package nl.hannahsten.texifyidea.bibreferencemanagers

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SyncZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        ApplicationManager.getApplication().invokeLater {
            GlobalScope.launch {
                ZoteroReferenceManager().getCollection(e.project!!)
            }
        }
    }
}