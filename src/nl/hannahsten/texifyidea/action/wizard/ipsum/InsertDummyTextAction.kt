package nl.hannahsten.texifyidea.action.wizard.ipsum

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.text.TexifyIpsumGenerator
import kotlin.random.Random

/**
 * @author Hannah Schellekens
 */
open class InsertDummyTextAction : AnAction() {

    /**
     * Opens and handles the dummy text UI.
     */
    private fun executeAction(file: PsiFile) {
        val project = file.project
        val editor = project.currentTextEditor()?.editor ?: return

        // Get the indentation from the current line.
        val indent = editor.document.lineIndentationByOffset(editor.caretOffset())

        // Create the dialog.
        val dialog = InsertDummyTextDialogWrapper()

        // If the user pressed OK, do stuff.
        if (dialog.showAndGet().not()) return
        val data = dialog.extractData()
        editor.insertDummyText(file, data, indent)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(PlatformDataKeys.PSI_FILE) ?: return
        executeAction(file)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val file = e.getData(PlatformDataKeys.PSI_FILE)
        val shouldDisplayMenu = file?.isLatexFile() == true
        e.presentation.isVisible = shouldDisplayMenu
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    private fun Editor.insertDummyText(file: PsiFile, data: DummyTextData, indent: String) = when (data.ipsumType) {
        DummyTextData.IpsumType.BLINDTEXT -> insertBlindtext(file, data)
        DummyTextData.IpsumType.LIPSUM -> insertLipsum(file, data)
        DummyTextData.IpsumType.RAW -> insertRaw(data, indent)
    }

    private fun Editor.insertBlindtext(file: PsiFile, data: DummyTextData) {
        // Import blindtext
        WriteCommandAction.runWriteCommandAction(file.project) {
            file.insertUsepackage(LatexPackage.BLINDTEXT)
        }

        // When itemize/enumerate/description is selected the level can be selected as well when larger than 1.
        val type = data.blindtextType
        if ((
            type == DummyTextData.BlindtextType.ITEMIZE || type == DummyTextData.BlindtextType.ENUMERATE ||
                type == DummyTextData.BlindtextType.DESCRIPTION
            ) && data.blindtextLevel > 1
        ) {
            val command = "\\" + type.commandNoSlash.replace("list", "listlist[${data.blindtextLevel}]")
            insertAtCaretAndMove(command)
            return
        }

        // When a paragraph only has repetitions (so no paragraphs), use the base \blindtext[n] command.
        if (type == DummyTextData.BlindtextType.PARAGRAPH && data.blindtextParagraphs == 1 && data.blindtextRepetitions > 1) {
            insertAtCaretAndMove("\\blindtext[${data.blindtextRepetitions}]")
            return
        }

        // When a pragraph has also paragraphs, use the \Blindtext[paragraphs][repetitions] version,
        if (type == DummyTextData.BlindtextType.PARAGRAPH && data.blindtextParagraphs > 1) {
            insertAtCaretAndMove("\\Blindtext[${data.blindtextParagraphs}][${data.blindtextRepetitions}]")
            return
        }

        // Otherwise, there is no special treatment needed, so just insert the command.
        val baseCommand = data.blindtextType.commandNoSlash
        insertAtCaretAndMove("\\$baseCommand")
    }

    private fun Editor.insertLipsum(file: PsiFile, data: DummyTextData) {
        // Import blindtext
        WriteCommandAction.runWriteCommandAction(file.project) {
            file.insertUsepackage(LatexPackage.LIPSUM)
        }

        val star = if (data.lipsumParagraphSeparator == DummyTextData.LipsumParagraphSeparation.SPACE) "*" else ""

        // By default, \lipsum prints the first 7 paragraphs.
        if (data.lipsumParagraphs == 1..7 && data.lipsumSentences == 1..999) {
            insertAtCaretAndMove("\\lipsum$star")
            return
        }

        // When the sentences is at max value (999), assume no specific sentences are selected => only first optional
        // parameter.
        val paragraphRange = data.lipsumParagraphs.toRangeString()
        if (data.lipsumSentences == 1..999) {
            insertAtCaretAndMove("\\lipsum$star[$paragraphRange]")
            return
        }

        // Otherwise, specify both paragraphs and sentences.
        val sentenceRange = data.lipsumSentences.toRangeString()
        insertAtCaretAndMove("\\lipsum$star[$paragraphRange][$sentenceRange]")
    }

    private fun Editor.insertRaw(data: DummyTextData, indent: String) {
        val generator = TexifyIpsumGenerator(
            data.rawParagraphs, data.rawSentencesPerParagraph, Random(data.rawSeed), data.rawDummyTextType
        )
        val dummyText = generator.generate()

        val result = StringBuilder()
        var currentIndent = ""
        dummyText.forEach { paragraph ->
            paragraph.forEach { sentence ->
                result.append(currentIndent).append(sentence).append("\n")
                currentIndent = indent
            }
            result.append("\n")
        }

        insertAtCaretAndMove(result.toString().trimEnd())
    }
}