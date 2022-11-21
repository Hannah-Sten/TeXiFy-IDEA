package nl.hannahsten.texifyidea.editor

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import com.intellij.openapi.project.Project
import nl.hannahsten.texifyidea.action.insert.InsertStyledText
import nl.hannahsten.texifyidea.editor.pasteproviders.PandocStandaloneDialog
import nl.hannahsten.texifyidea.editor.pasteproviders.htmlTextIsFormatable
import nl.hannahsten.texifyidea.editor.pasteproviders.parseToString
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.Log
import nl.hannahsten.texifyidea.util.PackageUtils.insertPreambleText
import nl.hannahsten.texifyidea.util.PandocUtil
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.psiFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.datatransfer.DataFlavor

/**
 * Pastes html and applies text(bf|it)
 *
 * @author jojo2357
 */
class StyledTextPasteProvider : PasteProvider {

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false
        if (ShiftTracker.isShiftPressed()) return false

        val pasteData = dataContext.transferableHtml() ?: return false
        Log.warn("Attempting to paste $pasteData")

        return htmlTextIsFormatable(pasteData)
    }

    override fun performPaste(dataContext: DataContext) {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardHtml = dataContext.transferableHtml() ?: return
        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml)

        val tableTextToInsert = Jsoup.parse(html).parseText(project, dataContext)

        val editor = dataContext.getData(PlatformDataKeys.PROJECT)?.currentTextEditor() ?: return

        InsertStyledText(tableTextToInsert).actionPerformed(file, project, editor)
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext)

    /**
     * Extracts the HTML on clipboard if there is HTML on the clipboard.
     */
    private fun DataContext.transferableHtml(): String? {
        val pasteData = getData(PasteAction.TRANSFERABLE_PROVIDER)?.produce() ?: return null
        return if (pasteData.isDataFlavorSupported(DataFlavor.allHtmlFlavor)) {
            pasteData.getTransferData(DataFlavor.allHtmlFlavor) as String
        }
        else null
    }


    /**
     * Creates the Table Creation Dialog filled in with the data from the clipboard.
     */
    private fun Document.parseText(project: Project, dataContext: DataContext): String {
        return if (PandocUtil.isPandocInPath) {
            val pandocStandaloneDialog = PandocStandaloneDialog()
            if (pandocStandaloneDialog.abort)
                parseToString(select("body")[0].childNodes(), project, dataContext)
            else {
                val isStandalone: Boolean = pandocStandaloneDialog.isAddImports ?: return ""

                val out = PandocUtil.translateHtml(this.html(), isStandalone)

                if (out == null)
                    parseToString(select("body")[0].childNodes(), project, dataContext)
                else {
                    if (out.first is String)
                        insertPreambleText(
                            dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)
                                ?.psiFile(dataContext.getData(PlatformDataKeys.PROJECT)!!)!!,
                            out.first!!
                        )
                    out.second
                }
            }
        }
        else
            parseToString(select("body")[0].childNodes(), project, dataContext)
    }
}