package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.actions.PasteAction
import com.intellij.openapi.project.Project
import com.intellij.testFramework.utils.vfs.getDocument
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.PackageUtils.insertPreambleText
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.psiFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.datatransfer.DataFlavor

/**
 * Takes html from clipboard and pastes it as LaTeX.
 * todo why is there no StyledTextPasteProvider subclassing LatexPasteProvider
 *
 * @author jojo2357
 */
class StyledTextPasteProvider : PasteProvider {

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false

        val pasteData = dataContext.transferableHtml() ?: return false

        return htmlTextIsFormattable(pasteData)
    }

    override fun performPaste(dataContext: DataContext) {
        val file = dataContext.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardHtml = dataContext.transferableHtml() ?: return
        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml)

        val textToInsert = Jsoup.parse(html).parseText(project, dataContext)

        val editor = dataContext.getData(PlatformDataKeys.PROJECT)?.currentTextEditor() ?: return

        // todo insert properly
        file.getDocument().insertString(editor.editor.caretModel.offset, textToInsert)
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext)

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

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
     * Parses html to LaTeX using pandoc if available, otherwise defaults to built-in methods.
     *
     * @return Translated LaTeX
     */
    private fun Document.parseText(project: Project, dataContext: DataContext): String {
        val default = parseToString(select("body")[0].childNodes(), project, dataContext)
        return if (!PandocPasteProvider.isPandocInPath) {
            default
        }
        else {
            // todo why put a dialog?
            val pandocStandaloneDialog = PandocStandaloneDialog()
            if (pandocStandaloneDialog.abort)
                default
            else {
                val isStandalone: Boolean = pandocStandaloneDialog.isAddImports ?: return default

                val latexText = PandocPasteProvider(isStandalone).translateHtml(this.html()) ?: return default

                if ("\\begin{document}" in latexText) {
                    val (preamble, content) = latexText.split("\\begin{document}")
                    insertPreambleText(
                        dataContext.getData(PlatformDataKeys.VIRTUAL_FILE)
                            ?.psiFile(dataContext.getData(PlatformDataKeys.PROJECT)!!)!!,
                        preamble
                    )
                    content
                }
                else {
                    latexText
                }
            }
        }
    }
}