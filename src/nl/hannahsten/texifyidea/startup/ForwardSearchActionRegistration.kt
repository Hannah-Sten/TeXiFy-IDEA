package nl.hannahsten.texifyidea.startup

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import nl.hannahsten.texifyidea.action.ForwardSearchActionBase
import nl.hannahsten.texifyidea.run.linuxpdfviewer.InternalPdfViewer
import nl.hannahsten.texifyidea.run.pdfviewer.ExternalPdfViewers
import javax.swing.KeyStroke

/**
 * Register forward search actions dynamically so forward search actions of external pdfs can also be registered.
 */
class ForwardSearchActionRegistration : StartupActivity, DumbAware {

    @Synchronized
    override fun runActivity(project: Project) {
        val actions = getForwardSearchActions()
        val group = getActionGroup() ?: return
        val shortCut = KeyboardShortcut(KeyStroke.getKeyStroke("control alt shift PERIOD"), null)
        for (a in actions) {
            KeymapManager.getInstance().activeKeymap.addShortcut(a.id, shortCut)
            ActionManager.getInstance().registerAction(a.id, a)
            group.add(a)
        }
    }

    private fun getActionGroup() = ActionManager.getInstance().getAction("texify.LatexMenuTools") as? DefaultActionGroup

    private fun getForwardSearchActions(): List<ForwardSearchActionBase> =
        (InternalPdfViewer.availableSubset().filter { it != InternalPdfViewer.NONE } +
                ExternalPdfViewers.getExternalPdfViewers().toSet())
            .map { ForwardSearchActionBase(it) }

    fun unload() {
        val actions = getForwardSearchActions()
        val group = getActionGroup() ?: return
        for (a in actions) {
            ActionManager.getInstance().unregisterAction(a.id)
            group.remove(a)
        }
    }
}