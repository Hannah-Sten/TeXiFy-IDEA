package nl.hannahsten.texifyidea.blame

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.util.Consumer
import java.awt.Component
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Send error report to GitHub issue tracker.
 *
 * @author Sten Wessel
 */
class LatexErrorReportSubmitter : ErrorReportSubmitter() {

    companion object {

        private const val URL = "https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new?labels=crash-report&title="
        private const val ENCODING = "UTF-8"
    }

    override fun getReportActionText() = "Report to TeXiFy-IDEA issue tracker"

    override fun submit(events: Array<IdeaLoggingEvent>, additionalInfo: String?,
                        parentComponent: Component, consumer: Consumer<SubmittedReportInfo>): Boolean {
        val event = events.firstOrNull()
        val title = event?.throwableText?.lineSequence()?.first()
                ?: event?.message
                ?: "Crash Report: <Fill in title>"
        val body = event?.throwableText ?: "Please paste the full stacktrace from the IDEA error popup."

        val builder = StringBuilder(URL)
        try {
            builder.append(URLEncoder.encode(title, ENCODING))
            builder.append("&body=")
            builder.append(URLEncoder.encode("### Description\n", ENCODING))
            builder.append(URLEncoder.encode(additionalInfo ?: "\n", ENCODING))
            builder.append(URLEncoder.encode("\n\n### Stacktrace\n```\n${body.take(7000)}\n```", ENCODING))
        }
        catch (e: UnsupportedEncodingException) {
            consumer.consume(SubmittedReportInfo(
                    null,
                    null,
                    SubmittedReportInfo.SubmissionStatus.FAILED
            ))
            return false
        }

        BrowserUtil.browse(builder.toString())
        consumer.consume(SubmittedReportInfo(
                null,
                "GitHub issue",
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
        ))
        return true
    }
}