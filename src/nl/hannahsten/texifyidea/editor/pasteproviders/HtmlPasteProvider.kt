package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.actions.PasteAction
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.PackageUtils.insertPreambleText
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.psiFile
import org.jsoup.Jsoup
import org.jsoup.nodes.Node
import java.awt.datatransfer.DataFlavor

/**
 * Takes html from clipboard and pastes it as LaTeX.
 *
 * @author jojo2357
 */
class HtmlPasteProvider : PasteProvider {

    override fun isPastePossible(dataContext: DataContext): Boolean {
        val file = dataContext.getData(PlatformDataKeys.PSI_FILE) ?: return false
        if (file.isLatexFile().not()) return false

        val pasteData = dataContext.transferableHtml() ?: return false

        return htmlTextIsFormattable(pasteData)
    }

    override fun performPaste(dataContext: DataContext) {
        val project = dataContext.getData(PlatformDataKeys.PROJECT) ?: return
        val clipboardHtml = dataContext.transferableHtml() ?: return
        val editor = project.currentTextEditor()?.editor ?: return

        // dont bother with expensive operations unless the gimmes have passed

        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml)
        val textToInsert = convertHtmlToLatex(Jsoup.parse(html).select("body")[0], dataContext)

        val writeAction = Runnable { EditorModificationUtil.insertStringAtCaret(editor, textToInsert) }
        WriteCommandAction.runWriteCommandAction(project, writeAction)
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
     * Use various converters to convert all the tables, image references and styled text to LaTeX.
     * todo this we can unit test
     */
    private fun convertHtmlToLatex(htmlIn: Node, dataContext: DataContext): String {
        // could be inlined, but kept out for neatness
        fun default() = parseToString(htmlIn.childNodes(), dataContext)

        return if (!PandocPasteTranslator.isPandocInPath) {
            default()
        }
        else {
            // Pandoc is available, so we ask the user for their preference
            // todo make sure to ask only once and add a setting
            val pandocStandaloneDialog = PandocStandaloneDialog()
            if (pandocStandaloneDialog.abort)
                default()
            else {
                val isStandalone: Boolean = pandocStandaloneDialog.isAddImports ?: return default()

                val latexText = PandocPasteTranslator(isStandalone).translateHtml(htmlIn.toString()) ?: return default()

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