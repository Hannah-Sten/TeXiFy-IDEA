package nl.hannahsten.texifyidea.action.library.local

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import nl.hannahsten.texifyidea.action.library.AddLibraryAction
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.LocalBibLibrary
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import javax.swing.JComponent

class AddLocalLibraryAction : AddLibraryAction<LocalBibLibrary, AddLocalLibraryAction.AddLocalLibraryWrapper>() {
    class AddLocalLibraryWrapper(val project: Project) : AddLibDialogWrapper("local") {
        override fun createCenterPanel(): JComponent? {
            TODO("Not yet implemented")
        }
    }

    override fun getDialog(project: Project): AddLocalLibraryWrapper {
        return AddLocalLibraryWrapper(project)
    }

    override suspend fun createLibrary(dialogWrapper: AddLocalLibraryWrapper, project: Project): Pair<LocalBibLibrary, List<BibtexEntry>>? {
        TODO("Not yet implemented")
    }
}
