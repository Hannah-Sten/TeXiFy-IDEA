package nl.hannahsten.texifyidea.remotelibraries

import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import nl.hannahsten.texifyidea.psi.BibtexEntry
import javax.swing.JComponent

class AddZoteroAction : AddLibraryAction<ZoteroLibrary, AddZoteroAction.AddZoteroDialogWrapper>() {

    override fun getDialog(project: Project): AddZoteroDialogWrapper {
        return AddZoteroDialogWrapper(project)
    }

    override suspend fun createLibrary(dialogWrapper: AddZoteroDialogWrapper, project: Project): Pair<ZoteroLibrary, List<BibtexEntry>> {
        val library = ZoteroLibrary(dialogWrapper.userID, dialogWrapper.userApiKey)
        val credentials = Credentials(dialogWrapper.userID, dialogWrapper.userApiKey)
        PasswordSafe.instance.set(ZoteroLibrary.credentialAttributes, credentials)
        val bibItems = library.getCollection()
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
        return library to bibItems
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
                    contextHelp("You can find your user ID in Zotero Settings > Feeds/API.")
                }
                row("User API key:") {
                    textField().bindText({ userApiKey }, { userApiKey = it })
                    contextHelp("Create a new API key in Zotero Settings > Feeds/API > Create new private key")
                }
            }
        }
    }
}