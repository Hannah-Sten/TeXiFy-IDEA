package nl.hannahsten.texifyidea.run.ui.console.logtab

import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.DUPLICATE_WHITESPACE
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.LINE_WIDTH
import nl.hannahsten.texifyidea.run.ui.console.logtab.LatexLogMagicRegex.TEX_MISC_WARNINGS
import nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.errors.LatexErrorHandler
import nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.errors.LatexFixMeErrorMessageHandler
import nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.errors.LatexSingleLineErrorMessageHandler
import nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.errors.LatexUndefinedControlSequenceHandler
import nl.hannahsten.texifyidea.run.ui.console.logtab.messagehandlers.warnings.*
import nl.hannahsten.texifyidea.util.remove

object LatexLogMessageExtractor {

    /**
     * Pre-processing to check if line is worth looking at.
     */
    fun skip(text: String?): Boolean {
        return text.isNullOrBlank()
    }

    /**
     * Look for a warning or error message in [text], and return a handler that
     * can handle the warning (i.e., process it and output the correct log message).
     * Return null if [text] does not contain such an error or warning.
     *
     * @param text Text in which to search for error messages (will be two consecutive lines)
     * @param newText Second line of 'text',
     */
    fun findMessage(text: String, newText: String, currentFile: String?): LatexLogMessage? {
        val specialErrorHandlersList = listOf(
            LatexUndefinedControlSequenceHandler,
            LatexSingleLineErrorMessageHandler,
            LatexFixMeErrorMessageHandler
        )

        // Most of these are just to have a special regex to extract the right line number
        val specialWarningHandlersList = listOf(
            YouHaveRequestedOnInputLineVersionOfPackageWarningHandler,
            OverfullHboxWarningHandler,
            EndOccurredInsideGroupWarningHandler,
            EndOccurredWhenConditionWasIncompleteWarningHandler,
            LatexPackageWarningHandler,
            LatexReferenceCitationWarningHandler,
            LatexLineWarningHandler,
            LatexFixMeWarningMessageHandler,
            LatexPdftexWarningMessageHandler
        )
        // Problem: if we just joined the two lines together, we wouldn't know if the first line actually is part of (for example)
        // the filepath, and we might match on things like /home/.../package.sty\n/home/.../otherpackage.sty:42: Error Message
        // Proposal: only join the lines if the first line was broken by line length
        // Note that if it wasn't, we match the first line (not the second) because we will encounter the second line in the next iteration
        val textToMatch = if (text.removeSuffix(newText).length < LINE_WIDTH - 1) text.removeSuffix(newText) else text

        // Look for errors that need special treatment.
        specialErrorHandlersList.forEach { handler ->
            if (handler.regex.any { it.containsMatchIn(text) }) {
                return handler.findMessage(text, newText, currentFile)
            }
        }

        if (LatexErrorHandler.regex.any { it.containsMatchIn(textToMatch) }) {
            return LatexErrorHandler.findMessage(text, newText, currentFile)
        }

        // Look for warnings that need special treatment.
        specialWarningHandlersList.forEach { handler ->
            // Check if the match starts in 'text', because if not then we will encounter it again the next time
            if (handler.regex.any { r ->
                r.containsMatchIn(text) &&
                    r.find(text)?.range?.start?.let { it <= text.removeSuffix(newText).length - 1 } == true
            }
            ) {
                return handler.findMessage(text, newText, currentFile)
            }
        }

        // Check if we have found a warning
        val firstLine = text.removeSuffix(newText)
        if (TEX_MISC_WARNINGS.any { firstLine.startsWith(it) }) {
            // Don't include the second line if it is not part of the message
            // (Do this before cleaning up the messageText)
            var messageText = if (LatexOutputListener.isLineEndOfMessage(newText, firstLine)) firstLine.trim() else text

            messageText = messageText.remove("LaTeX Warning: ")
                // Improves readability, and at the moment we don't have an example where this would be incorrect
                .trim('(', ')', '[', ']')
                .replace(DUPLICATE_WHITESPACE.toRegex(), " ")

            return LatexLogMessage(
                messageText,
                fileName = currentFile,
                type = LatexLogMessageType.WARNING
            )
        }

        return null
    }
}