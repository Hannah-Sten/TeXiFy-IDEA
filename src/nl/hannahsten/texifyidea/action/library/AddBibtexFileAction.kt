package nl.hannahsten.texifyidea.action.library

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import nl.hannahsten.texifyidea.RemoteLibraryRequestFailure
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.BibtexFileLibrary
import nl.hannahsten.texifyidea.remotelibraries.RemoteBibLibraryFactory
import nl.hannahsten.texifyidea.remotelibraries.RemoteLibraryManager
import nl.hannahsten.texifyidea.ui.remotelibraries.AddLibDialogWrapper
import nl.hannahsten.texifyidea.util.CredentialAttributes
import javax.swing.JComponent

/**
 * Action to add a [BibtexFileLibrary] to the libraries tool window.
 */
class AddBibtexFileAction : AddLibraryAction<BibtexFileLibrary, AddBibtexFileAction.AddBibtexFileDialogWrapper>() {

    class AddBibtexFileDialogWrapper(val project: Project) : AddLibDialogWrapper(BibtexFileLibrary.NAME) {

        var path: String = ""

        init {
            init()
        }

        override fun createCenterPanel(): JComponent {
            val browseField = TextFieldWithBrowseButton().apply {
                addBrowseFolderListener(
                    TextBrowseFolderListener(
                        FileChooserDescriptor(true, false, false, false, false, false)
                            .withFileFilter { it.extension == "bib" }
                            .withTitle("Select BibTeX File")
                    )
                )
            }

            return panel {
                row("Path to BibTeX file:") {
                    cell(browseField).bindText({ path }, { path = it }).focused()
                    contextHelp("Select a local path to a .bib file")
                }
            }
        }
    }

    override fun getDialog(project: Project): AddBibtexFileDialogWrapper {
        return AddBibtexFileDialogWrapper(project)
    }

    override suspend fun createLibrary(dialogWrapper: AddBibtexFileDialogWrapper, project: Project): Either<RemoteLibraryRequestFailure, Pair<BibtexFileLibrary, List<BibtexEntry>>?> = either {
        val library = RemoteBibLibraryFactory.create<BibtexFileLibrary>(BibtexFileLibrary.NAME) ?: return@either null
        val path = Credentials(dialogWrapper.path)
        PasswordSafe.instance.set(CredentialAttributes.BibtexFile.path, path)
        val bibItems = library.getCollection().getOrElse { raise(it) }
        RemoteLibraryManager.getInstance().updateLibrary(library, bibItems)
        library to bibItems
    }
}