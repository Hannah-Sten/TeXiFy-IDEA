package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab

import com.intellij.diagnostic.logging.AdditionalTabComponent
import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.BorderLayout
import javax.swing.JComponent

/**
 * todo remove
 * Similar to [nl.hannahsten.texifyidea.run.latex.logtab.LatexLogTabComponent].
 */
class BibtexLogTabComponent(val project: Project, val mainFile: VirtualFile?, startedProcess: ProcessHandler) : AdditionalTabComponent(BorderLayout()) {

    private val bibtexMessageList = mutableListOf<BibtexLogMessage>()
//    private val treeView = LatexCompileMessageTreeView(project, mutableListOf(), bibtexMessageList)

    init {
//        add(treeView, BorderLayout.CENTER)
//        startedProcess.addProcessListener(BibtexOutputListener(project, mainFile, bibtexMessageList, treeView), this)
    }

    override fun getTabTitle() = "Log messages"

    override fun dispose() {
    }

    override fun getPreferredFocusableComponent() = component

    override fun getToolbarActions(): ActionGroup? = null

    override fun getToolbarContextComponent(): JComponent? = null

    override fun getToolbarPlace(): String? = null

    override fun getSearchComponent(): JComponent? = null

    override fun isContentBuiltIn() = false
}
