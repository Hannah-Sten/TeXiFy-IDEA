package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.swing.JComponent

class AddZoteroAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dialogWrapper = AddZoteroDialogWrapper(e.project ?: return)

        if(dialogWrapper.showAndGet()) {
            ApplicationManager.getApplication().invokeLater {
                CoroutineScope(Dispatchers.Default).launch {
                    val library = ZoteroLibrary(dialogWrapper.userID, dialogWrapper.userApiKey)
                    val bibItems = library.getCollection(e.project!!)
                    RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
                    println(RemoteLibraryManager.getInstance().libraries[library.name]?.size)
                }
            }
        }
    }

    class AddZoteroDialogWrapper(val project: Project) : DialogWrapper(true) {
        var userID: String = ""

        var userApiKey: String = ""

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row("User ID:") {
                    textField().bindText({ userID }, { userID = it })
                }
                row("User API key:") {
                    textField().bindText({ userApiKey }, { userApiKey = it })
                }
            }
        }
    }
}