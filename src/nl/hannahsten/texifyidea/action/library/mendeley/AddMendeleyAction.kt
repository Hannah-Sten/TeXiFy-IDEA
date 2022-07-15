package nl.hannahsten.texifyidea.action.library.mendeley

import com.intellij.openapi.project.Project
import com.intellij.ui.jcef.JBCefBrowser
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.action.library.AddLibraryAction
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyAuthenticator
import nl.hannahsten.texifyidea.remotelibraries.mendeley.MendeleyLibrary
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
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

    class AddMendeleyDialogWrapper(
        val project: Project,
    ) : AddLibDialogWrapper(MendeleyLibrary.NAME) {

        private val browser = JBCefBrowser(MendeleyAuthenticator.authorizationUrl)

        init {
            init()
            if (!MendeleyAuthenticator.serverRunning) MendeleyAuthenticator.authorizationServer.start(false)
        }

        override fun createCenterPanel(): JComponent {
            return browser.component
        }
    }
}