package nl.hannahsten.texifyidea.editor

import com.intellij.formatting.FormatConstants
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataContextWrapper
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LanguageLineWrapPositionStrategy
import com.intellij.openapi.editor.ex.util.EditorFacadeImpl
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.util.MathUtil
import com.intellij.util.text.CharArrayUtil
import java.awt.Font
import kotlin.math.max
import kotlin.math.min

/**
 * This class has most code copied from EditorFacade, which has internal API status.
 */
object LatexLineWrapper {

    fun doWrapLongLinesIfNecessary(
        editor: Editor, project: Project, document: Document,
        startOffset: Int, endOffset: Int, enabledRanges: List<TextRange>, rightMargin: Int
    ) {
        // Normalization.
        val startOffsetToUse = MathUtil.clamp(startOffset, 0, document.textLength)
        var endOffsetToUse = MathUtil.clamp(endOffset, 0, document.textLength)
        val strategy = LanguageLineWrapPositionStrategy.INSTANCE.forEditor(editor)
        val text = document.charsSequence
        val startLine = document.getLineNumber(startOffsetToUse)
        val endLine = document.getLineNumber(0.coerceAtLeast(endOffsetToUse - 1))
        var maxLine = document.lineCount.coerceAtMost(endLine + 1)
        var tabSize = EditorUtil.getTabSize(editor)
        if (tabSize <= 0) {
            tabSize = 1
        }
        val spaceSize = EditorUtil.getSpaceWidth(Font.PLAIN, editor)
        val shifts = listOf(0, 0)
        // shifts[0] - lines shift.
        // shift[1] - offset shift.
        var cumulativeShift = 0
        for (line in startLine until maxLine) {
            val startLineOffset = document.getLineStartOffset(line)
            val endLineOffset = document.getLineEndOffset(line)
            if (!canWrapLine(
                    startOffsetToUse.coerceAtLeast(startLineOffset),
                    endOffsetToUse.coerceAtMost(endLineOffset),
                    cumulativeShift,
                    enabledRanges
                )
            ) {
                continue
            }
            val preferredWrapPosition = calculatePreferredWrapPosition(
                editor,
                text,
                tabSize,
                spaceSize,
                startLineOffset,
                endLineOffset,
                endOffsetToUse,
                rightMargin
            )
            if (preferredWrapPosition < 0 || preferredWrapPosition >= endLineOffset) {
                continue
            }
            if (preferredWrapPosition >= endOffsetToUse) {
                return
            }

            // We know that current line exceeds right margin if control flow reaches this place, so, wrap it.
            val wrapOffset = strategy.calculateWrapPosition(
                document, editor.project, max(startLineOffset, startOffsetToUse), min(endLineOffset, endOffsetToUse),
                preferredWrapPosition, false, false
            )
            if (wrapOffset < 0 // No appropriate wrap position is found.
                // No point in splitting line when its left part contains only white spaces, example:
                //    line start -> |                   | <- right margin
                //                  |   aaaaaaaaaaaaaaaa|aaaaaaaaaaaaaaaaaaaa() <- don't want to wrap this line even if it exceeds right margin
                || CharArrayUtil.shiftBackward(text, startLineOffset, wrapOffset - 1, " \t") < startLineOffset
            ) {
                continue
            }

            // Move caret to the target position and emulate pressing <enter>.
            editor.caretModel.moveToOffset(wrapOffset)
            emulateEnter(editor, project, shifts)

            //If number of inserted symbols on new line after wrapping more or equal then symbols left on previous line
            //there was no point to wrapping it, so reverting to before wrapping version
            if (shifts[1] - 1 >= wrapOffset - startLineOffset) {
                document.deleteString(wrapOffset, wrapOffset + shifts[1])
            }
            else {
                // We know that number of lines is just increased, hence, update the data accordingly.
                maxLine += shifts[0]
                endOffsetToUse += shifts[1]
                cumulativeShift += shifts[1]
            }
        }
    }
    
    private fun canWrapLine(startOffset: Int, endOffset: Int, offsetShift: Int, enabledRanges: List<TextRange>): Boolean {
        for (range in enabledRanges) {
            if (range.containsOffset(startOffset - offsetShift) && range.containsOffset(endOffset - offsetShift)) return true
        }
        return false
    }

    /**
     * Checks if it's worth to try to wrap target line (it's long enough) and tries to calculate preferred wrap position.
     *
     * @param editor                target editor
     * @param text                  text contained at the given editor
     * @param tabSize               tab space to use (number of visual columns occupied by a tab)
     * @param spaceSize             space width in pixels
     * @param startLineOffset       start offset of the text line to process
     * @param endLineOffset         end offset of the text line to process
     * @param targetRangeEndOffset  target text region's end offset
     * @return                      negative value if no wrapping should be performed for the target line;
     * preferred wrap position otherwise
     */
    private fun calculatePreferredWrapPosition(
        editor: Editor,
        text: CharSequence,
        tabSize: Int,
        spaceSize: Int,
        startLineOffset: Int,
        endLineOffset: Int,
        targetRangeEndOffset: Int,
        rightMargin: Int
    ): Int {
        var hasTabs = false
        var canOptimize = true
        var hasNonSpaceSymbols = false
        loop@ for (i in startLineOffset until min(endLineOffset, targetRangeEndOffset)) {
            when (text[i]) {
                '\t' -> {
                    hasTabs = true
                    if (hasNonSpaceSymbols) {
                        canOptimize = false
                        break@loop
                    }
                }

                ' ' -> {}
                else -> hasNonSpaceSymbols = true
            }
        }
        val reservedWidthInColumns = FormatConstants.getReservedLineWrapWidthInColumns(editor)
        return if (!hasTabs) {
            wrapPositionForTextWithoutTabs(startLineOffset, endLineOffset, targetRangeEndOffset, reservedWidthInColumns, rightMargin)
        }
        else if (canOptimize) {
            wrapPositionForTabbedTextWithOptimization(
                text, tabSize, startLineOffset, endLineOffset, targetRangeEndOffset,
                reservedWidthInColumns, rightMargin
            )
        }
        else {
            wrapPositionForTabbedTextWithoutOptimization(
                editor, text, spaceSize, startLineOffset, endLineOffset, targetRangeEndOffset,
                reservedWidthInColumns, rightMargin
            )
        }
    }

    private fun wrapPositionForTextWithoutTabs(
        startLineOffset: Int, endLineOffset: Int, targetRangeEndOffset: Int,
        reservedWidthInColumns: Int, rightMargin: Int
    ): Int {
        return if (min(endLineOffset, targetRangeEndOffset) - startLineOffset > rightMargin) {
            startLineOffset + rightMargin - reservedWidthInColumns
        }
        else -1
    }

    private fun wrapPositionForTabbedTextWithOptimization(
        text: CharSequence,
        tabSize: Int,
        startLineOffset: Int,
        endLineOffset: Int,
        targetRangeEndOffset: Int,
        reservedWidthInColumns: Int,
        rightMargin: Int
    ): Int {
        var width = 0
        var symbolWidth: Int
        var result = Int.MAX_VALUE
        var wrapLine = false
        for (i in startLineOffset until min(endLineOffset, targetRangeEndOffset)) {
            val c = text[i]
            symbolWidth = if (c == '\t') tabSize - width % tabSize else 1
            if (width + symbolWidth + reservedWidthInColumns >= rightMargin
                && min(endLineOffset, targetRangeEndOffset) - i >= reservedWidthInColumns
            ) {
                // Remember preferred position.
                result = i - 1
            }
            if (width + symbolWidth >= rightMargin) {
                wrapLine = true
                break
            }
            width += symbolWidth
        }
        return if (wrapLine) result else -1
    }

    private fun wrapPositionForTabbedTextWithoutOptimization(
        editor: Editor,
        text: CharSequence,
        spaceSize: Int,
        startLineOffset: Int,
        endLineOffset: Int,
        targetRangeEndOffset: Int,
        reservedWidthInColumns: Int,
        rightMargin: Int
    ): Int {
        var width = 0
        var x = 0
        var newX: Int
        var symbolWidth: Int
        var result = Int.MAX_VALUE
        var wrapLine = false
        for (i in startLineOffset until min(endLineOffset, targetRangeEndOffset)) {
            val c = text[i]
            if (c == '\t') {
                newX = EditorUtil.nextTabStop(x, editor)
                val diffInPixels = newX - x
                symbolWidth = diffInPixels / spaceSize
                if (diffInPixels % spaceSize > 0) {
                    symbolWidth++
                }
            }
            else {
                newX = x + EditorUtil.charWidth(c, Font.PLAIN, editor)
                symbolWidth = 1
            }
            if (width + symbolWidth + reservedWidthInColumns >= rightMargin
                && min(endLineOffset, targetRangeEndOffset) - i >= reservedWidthInColumns
            ) {
                result = i - 1
            }
            if (width + symbolWidth >= rightMargin) {
                wrapLine = true
                break
            }
            x = newX
            width += symbolWidth
        }
        return if (wrapLine) result else -1
    }

    private fun prepareContext(project: Project, editor: Editor): DataContext {
        // There is a possible case that formatting is performed from project view and editor is not opened yet. The problem is that
        // its data context doesn't contain information about project then. So, we explicitly support that here (see IDEA-72791).
        val editorProject = editor.project
        val context = EditorUtil.getEditorDataContext(editor)
        return if (editorProject != null) context
        else object : DataContextWrapper(context) {
            override fun getRawCustomData(dataId: String): Any? {
                return if (CommonDataKeys.PROJECT.`is`(dataId)) project else null
            }
        }
    }

    /**
     * Different from EditorFacade, this does not actually press enter, but wraps lines manually.
     * 
     * @author Thomas
     */
    fun emulateEnter(editor: Editor, project: Project, shifts: List<int>) {
        
    }
}