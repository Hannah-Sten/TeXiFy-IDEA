package nl.hannahsten.texifyidea.editor

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.fillParagraph.ParagraphFillHandler
import com.intellij.formatting.FormatterTagHandler
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UnfairTextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.nextLeaf
import com.intellij.psi.util.prevLeaf
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.file.LatexFile
import nl.hannahsten.texifyidea.psi.LatexBeginCommand
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexDisplayMath
import nl.hannahsten.texifyidea.psi.LatexEndCommand
import nl.hannahsten.texifyidea.util.magic.CommandMagic
import nl.hannahsten.texifyidea.util.parser.endOffset
import nl.hannahsten.texifyidea.util.parser.hasParent
import nl.hannahsten.texifyidea.util.parser.parentOfType

/**
 * Implements the paragraph fill handler action.
 *
 * @author Abby Berkers
 */
class LatexParagraphFillHandler : ParagraphFillHandler() {

    override fun isAvailableForFile(psiFile: PsiFile?): Boolean = psiFile is LatexFile

    /**
     * Given the leaf [element] at the caret at the time of invoking the fill paragraph action, replace the text of the
     * current paragraph with the same text such that the new text fills the editor evenly.
     *
     * Based on the super implementation, which wasn't custom enough. We now have finer control over when an element ends.
     */
    override fun performOnElement(element: PsiElement, editor: Editor) {
        val document = editor.document

        val (textRange, preFix, postFix) = getParagraphTextRange(element)
        if (textRange.isEmpty) return
        val text = textRange.substring(element.containingFile.text)

        val subStrings = StringUtil.split(text, "\n", true)
        // Join the paragraph text to a single line, so it can be wrapped later.
        val replacementText = preFix + subStrings.joinToString(" ") { it.trim() } + postFix

        CommandProcessor.getInstance().executeCommand(element.project, {
            document.replaceString(
                textRange.startOffset, textRange.endOffset,
                replacementText
            )
            val file = element.containingFile
            val formatterTagHandler = FormatterTagHandler(CodeStyle.getSettings(file))
            val enabledRanges = formatterTagHandler.getEnabledRanges(file.node, TextRange.create(0, document.textLength))

            // Don't simulate enter to wrap lines like EditorFacade does, as it has side effects (source: Yann)
            LatexLineWrapper.doWrapLongLinesIfNecessary(
                editor, document, textRange.startOffset,
                textRange.startOffset + replacementText.length + 1,
                enabledRanges,
                CodeStyle.getSettings(file).getRightMargin(element.language)
            )
        }, null, document)
    }

    /**
     * Get the text range of the paragraph of the current element, along with a prefix and postfix.
     *
     * Note that these paragraphs are different from the paragraphs as outputted by TeX. The paragraphs we are dealing
     * with here are the walls of text as they appear in the editor. E.g., these paragraphs are separated by a display
     * math environment, while for TeX a display math environment is part of the paragraph.
     */
    private fun getParagraphTextRange(element: PsiElement): Triple<TextRange, String, String> {
        // The final element of the paragraph.
        var endElement = element
        // The last element is the last element we checked. At the end the last element will be the first element that
        // is not part of the current paragraph.
        var lastElement = element.nextLeaf(true)
        while (lastElement != null && !separatesParagraph(lastElement)) {
            endElement = lastElement
            lastElement = lastElement.nextLeaf(true)
        }
        val postFix = if (endElement.isPostOrPreFix()) endElement.text else ""

        // The first element of the paragraph.
        var startElement = element
        // The last element that we checked. At the end the first element will be the last element that is not part of
        // the current paragraph, i.e., the paragraph starts at the next element after firstElement.
        var firstElement = element.prevLeaf(true)
        while (firstElement != null && !separatesParagraph(firstElement)) {
            startElement = firstElement
            firstElement = firstElement.prevLeaf(true)
        }
        val preFix = if (startElement.isPostOrPreFix()) startElement.text else ""

        return Triple(UnfairTextRange(startElement.startOffset, endElement.endOffset()), preFix, postFix)
    }

    /**
     * A paragraph ends when we encounter
     * - multiple new lines, or
     * - the begin of an environment (display math counts for this as well, inline math does not), or
     * - the end of the file.
     * In that case the current element is the first element that is not part of the paragraph anymore. If the last
     * element before the current element is a white space, that white space is still part of the paragraph.
     */
    private fun separatesParagraph(element: PsiElement): Boolean = when {
        element is PsiWhiteSpace -> element.text.count { it == '\n' } >= 2
        element.hasParent(LatexBeginCommand::class) -> true
        element.hasParent(LatexEndCommand::class) -> true
        element.hasParent(LatexDisplayMath::class) -> true
        element.hasParent(LatexCommands::class) -> {
            CommandMagic.sectionNameToLevel.contains(element.parentOfType(LatexCommands::class)?.commandToken?.text)
        }
        else -> false
    }

    /**
     * If the first or last element of a paragraph is a white space, this white space should be preserved.
     *
     * For example, when the paragraph is ended by an environment, the `\begin{env}` is the element that separates this
     * paragraph. The last element of the paragraph then is the new line before the `\begin{env}`. White spaces are
     * trimmed when creating the replacement text, but this white space should be preserved.
     */
    private fun PsiElement.isPostOrPreFix() = this is PsiWhiteSpace
}