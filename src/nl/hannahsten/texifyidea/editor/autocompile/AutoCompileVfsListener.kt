package nl.hannahsten.texifyidea.editor.autocompile

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.WindowManager
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings

/**
 * If a LaTeX file is saved, do automatic compilation if desired.
 *
 * @author Thomas
 */
class AutoCompileVfsListener : AsyncFileListener {

    override fun prepareChange(events: MutableList<out VFileEvent>): ChangeApplier? {
        if (!TexifySettings.getInstance().autoCompileOnSaveOnly || !events.any { it.file?.fileType == LatexFileType }) return null
        return object : ChangeApplier {
            override fun afterVfsChange() {
                super.afterVfsChange()
                // In order to figure out which run configuration should run, we currently just run the selected run configuration in the currently open project
                // suggestParentWindow needs to run in EDT
                runInEdt {
                    val project = ProjectManager.getInstance().openProjects.firstOrNull {
                        WindowManager.getInstance().suggestParentWindow(it)?.isActive == true
                    } ?: return@runInEdt
                    AutoCompileState.documentChanged(project)
                }
            }
        }
    }
}