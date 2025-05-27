package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.TexifyCoroutine

/**
 * If a LaTeX file is saved, do automatic compilation if desired.
 *
 * @author Thomas
 */
class AutoCompileVfsListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): ChangeApplier? {
        if (TexifySettings.getInstance().autoCompileOption != TexifySettings.AutoCompile.AFTER_DOCUMENT_SAVE || !events.any { it.file?.fileType == LatexFileType }) return null
        return object : ChangeApplier {
            override fun afterVfsChange() {
                super.afterVfsChange()
                // In order to figure out which run configuration should run, we currently just run the selected run configuration in the currently open project
                // suggestParentWindow needs to run in EDT
                TexifyCoroutine.runInBackground {
                    val project =  withContext(Dispatchers.EDT) {
                        ProjectManager.getInstance().openProjects.firstOrNull {
                            WindowManager.getInstance().suggestParentWindow(it)?.isActive == true
                        }
                    } ?: return@runInBackground // No active project, so no auto compilation
                    AutoCompileState.requestAutoCompilation(project)
                    // This should not run in EDT, because getting a RunManager instance will run blocking, which means we may get a deadlock if something else requests a read action (#3931)
                }
            }
        }
    }
}