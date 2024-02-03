package nl.hannahsten.texifyidea.editor.pasteproviders

import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.actions.PasteAction
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.Clipboard
import nl.hannahsten.texifyidea.util.currentTextEditor
import nl.hannahsten.texifyidea.util.files.isLatexFile
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
        val latexFile = dataContext.getData(PlatformDataKeys.PSI_FILE) as? LatexFile ?: return

        // dont bother with expensive operations unless the gimmes have passed

        val html = Clipboard.extractHtmlFromClipboard(clipboardHtml)
        val textToInsert = convertHtmlToLatex(Jsoup.parse(html).select("body")[0], latexFile)

        val writeAction = Runnable { EditorModificationUtil.insertStringAtCaret(editor, textToInsert) }
        WriteCommandAction.runWriteCommandAction(project, writeAction)
    }

    override fun isPasteEnabled(dataContext: DataContext) = isPastePossible(dataContext) && TexifySettings.getInstance().htmlPasteTranslator != TexifySettings.HtmlPasteTranslator.DISABLED

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
     */
    fun convertHtmlToLatex(htmlIn: Node, latexFile: LatexFile): String {
        return when(TexifySettings.getInstance().htmlPasteTranslator) {
            TexifySettings.HtmlPasteTranslator.BUILTIN -> convertHtmlToLatex(htmlIn.childNodes(), latexFile)
            TexifySettings.HtmlPasteTranslator.PANDOC -> PandocHtmlToLatexConverter().translateHtml(htmlIn.toString()) ?: convertHtmlToLatex(htmlIn.childNodes(), latexFile)
            TexifySettings.HtmlPasteTranslator.DISABLED -> htmlIn.toString() // Should not happen
        }
    }
}