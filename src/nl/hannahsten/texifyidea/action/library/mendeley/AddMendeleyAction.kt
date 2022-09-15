package nl.hannahsten.texifyidea.action.library.mendeley

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.action.library.AddLibraryAction
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyAuthenticator
import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyLibrary
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import javax.swing.JComponent

class AddMendeleyAction : AddLibraryAction<MendeleyLibrary, AddMendeleyAction.AddMendeleyDialogWrapper>() {

    override suspend fun createLibrary(dialogWrapper: AddMendeleyDialogWrapper, project: Project): Pair<MendeleyLibrary, List<BibtexEntry>>? {
        val library = RemoteBibLibraryFactory.create(MendeleyLibrary.NAME) as? MendeleyLibrary ?: return null
        val bibItems = library.getCollection()
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
        return library to bibItems
    }

    override fun getDialog(project: Project): AddMendeleyDialogWrapper {
        return AddMendeleyDialogWrapper(project)
    }

    /**
     * Reset the authorization server when the library is added, to prepare for the next time we want to add a library.
     */
    override fun onFinish() {
        MendeleyAuthenticator.reset()
    }

    class AddMendeleyDialogWrapper(
        val project: Project,
    ) : AddLibDialogWrapper(MendeleyLibrary.NAME) {

        private val browser = JBCefBrowser(MendeleyAuthenticator.authorizationUrl)

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            return browser.component
        }

        override fun doValidate(): ValidationInfo? {
            return if (MendeleyAuthenticator.isUserAuthenticationFinished) null
            else ValidationInfo("You are not yet logged in to Mendeley")
        }
    }
}