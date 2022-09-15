package nl.hannahsten.texifyidea.run.legacy

import com.intellij.diagnostic.logging.AdditionalTabComponent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.legacy.bibtex.logtab.BibtexLogMessage
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMessage
import java.awt.BorderLayout
import javax.swing.JComponent

/**
 * todo remove
 * Runner tab component displaying LaTeX log messages in a more readable and navigatable format.
 *
 * @param startedProcess The LaTeX compile process.
 *
 * @author Sten Wessel
 */
class LatexLogTabComponent(val project: Project, val mainFile: VirtualFile?, startedProcess: ProcessHandler) : AdditionalTabComponent(BorderLayout()) {

    private val latexMessageList = mutableListOf<LatexLogMessage>()
    // bibtex messages that need to be shown in the latex log tab (latexmk)
    private val bibtexMessageList = mutableListOf<BibtexLogMessage>()
//    private val treeView = LatexCompileMessageTreeView(project, latexMessageList, bibtexMessageList)

    init {
//        add(treeView, BorderLayout.CENTER)
//        startedProcess.addProcessListener(LatexOutputListener(project, mainFile, latexMessageList, bibtexMessageList, treeView), this)
    }

    override fun getTabTitle() = "Log Messages"

    override fun dispose() {}

    override fun getPreferredFocusableComponent() = component

    override fun getToolbarActions(): ActionGroup? = null

    override fun getToolbarContextComponent(): JComponent? = null

    override fun getToolbarPlace(): String = "top"

    override fun getSearchComponent(): JComponent? = null

    override fun isContentBuiltIn() = false
}
