package nl.hannahsten.texifyidea.action.library.zotero

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import nl.hannahsten.texifyidea.action.library.AddLibraryAction
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroGroupLibrary
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import nl.hannahsten.texifyidea.util.CredentialAttributes
import javax.swing.JComponent

class AddZoteroGroupAction : AddLibraryAction<ZoteroGroupLibrary, AddZoteroGroupAction.AddZoteroGroupDialogWrapper>() {

    override fun getDialog(project: Project): AddZoteroGroupDialogWrapper {
        return AddZoteroGroupDialogWrapper(project)
    }

    override suspend fun createLibrary(dialogWrapper: AddZoteroGroupDialogWrapper, project: Project): Either<RemoteLibraryRequestFailure, Pair<ZoteroGroupLibrary, List<BibtexEntry>>?> = either {
        val library = RemoteBibLibraryFactory.create<ZoteroGroupLibrary>(ZoteroGroupLibrary.NAME) ?: return@either null
        val credentials = Credentials(dialogWrapper.groupId, dialogWrapper.userApiKey)
        PasswordSafe.instance.set(CredentialAttributes.ZoteroGroup.groupAttributes, credentials)
        val bibItems = library.getCollection().getOrElse { raise(it) }
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
        library to bibItems
    }

    class AddZoteroGroupDialogWrapper(val project: Project) : AddLibDialogWrapper(ZoteroGroupLibrary.NAME) {

        var groupId: String = ""

        var userApiKey: String = ""

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row("Group ID:") {
                    textField().bindText({ groupId }, { groupId = it }).focused()
                    contextHelp("You can find the group ID in the URL of the group")
                }
                row("User API key:") {
                    textField().bindText({ userApiKey }, { userApiKey = it })
                    contextHelp("Create a new API key in Zotero Settings > Feeds/API > Create new private key")
                }
            }
        }
    }
}