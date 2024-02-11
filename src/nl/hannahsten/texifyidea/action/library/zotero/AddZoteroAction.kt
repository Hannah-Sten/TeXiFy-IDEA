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
import nl.hannahsten.texifyidea.remotelibraries.zotero.ZoteroLibrary
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import nl.hannahsten.texifyidea.util.CredentialAttributes.Zotero
import javax.swing.JComponent

class AddZoteroAction : AddLibraryAction<ZoteroLibrary, AddZoteroAction.AddZoteroDialogWrapper>() {

    override fun getDialog(project: Project): AddZoteroDialogWrapper {
        return AddZoteroDialogWrapper(project)
    }

    override suspend fun createLibrary(dialogWrapper: AddZoteroDialogWrapper, project: Project): Either<RemoteLibraryRequestFailure, Pair<ZoteroLibrary, List<BibtexEntry>>?> = either {
        val library = RemoteBibLibraryFactory.create<ZoteroLibrary>(dialogWrapper.name, dialogWrapper.url) ?: return@either null
        val credentials = Credentials(dialogWrapper.userID, dialogWrapper.userApiKey)
        PasswordSafe.instance.set(Zotero.userAttributes, credentials)
        val bibItems = library.getCollection().getOrElse { raise(it) }
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems, dialogWrapper.url)
        library to bibItems
    }

    class AddZoteroDialogWrapper(val project: Project) : AddLibDialogWrapper(ZoteroLibrary.NAME) {

        var name: String = "Zotero"

        // Other examples:
        // https://api.zotero.org/groups/2608283/items?format=biblatex
        // http://127.0.0.1:23119/better-bibtex/export/collection?/1/VXN7BVFN.biblatex
        var url: String = ZoteroLibrary.DEFAULT_URL

        var userID: String = ""

        var userApiKey: String = ""

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row("Name") {
                    textField().bindText({ name }, { name = it })
                    contextHelp("Name as shown in the TeXiFy Remote Library tool window")
                }
                row("URL") {
                    textField().bindText({ url }, { url = it }).resizableColumn()
                    contextHelp("Zotero API, see TeXiFy documentation for using groups or BBT")
                }
                row("User ID:") {
                    textField().bindText({ userID }, { userID = it }).focused()
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