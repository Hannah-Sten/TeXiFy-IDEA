package nl.hannahsten.texifyidea

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.intellij.ide.BrowserUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.showOkCancelDialog
import com.intellij.util.Consumer
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.awt.Component
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

/**
 * Send error report to GitHub issue tracker.
 *
 * @author Sten Wessel
 */
class LatexErrorReportSubmitter : ErrorReportSubmitter() {

    companion object {

        private const val URL = "https://github.com/Hannah-Sten/TeXiFy-IDEA/issues/new?labels=crash-report&title="
        private const val ENCODING = "UTF-8"

        private var latestVersionCached = ""

        fun getLatestVersion(): String {
            if (latestVersionCached.isNotBlank()) return latestVersionCached

            val url = URL("https://api.github.com/repos/Hannah-Sten/TeXiFy-IDEA/releases")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "GET"
                connectTimeout = 1000
                readTimeout = 1000

                val response = inputStream.bufferedReader().readLine()
                val releases = Parser.default().parse(StringBuilder(response)) as? JsonArray<*>
                latestVersionCached = (releases?.firstOrNull() as? JsonObject)?.string("tag_name") ?: ""
                return latestVersionCached
            }
        }
    }

    @Suppress("DialogTitleCapitalization")
    override fun getReportActionText() = "Report to TeXiFy-IDEA issue tracker"

    override fun submit(events: Array<out IdeaLoggingEvent>?, additionalInfo: String?, parentComponent: Component, consumer: Consumer<in SubmittedReportInfo>): Boolean {

        val currentVersion = PluginManagerCore.getPlugin(PluginId.getId("nl.rubensten.texifyidea"))?.version
        // Don't do the check when there's no internet connection
        val latestVersion = try {
            getLatestVersion()
        }
        catch (e: UnknownHostException) {
            currentVersion
        }

        if (latestVersion?.isNotBlank() == true && DefaultArtifactVersion(currentVersion) < DefaultArtifactVersion(latestVersion)) {

            JBPopupFactory.getInstance().createMessage("")
                .showInCenterOf(parentComponent)
            val result = showOkCancelDialog(
                "Update TeXiFy",
                "You are not using the latest version of TeXiFy, please update first (may require an IDE update).\n" +
                        "Go to Settings > Plugins to update. If no update is available, update your IDE first and then update TeXiFy.",
                "Cancel submit", // Sort of the wrong way around, but it suggests to cancel this way
                "Submit anyway"
            )

            if (result == Messages.OK) return false
        }

        submit(events, additionalInfo, consumer)
        return true
    }

    private fun submit(events: Array<out IdeaLoggingEvent>?, additionalInfo: String?, consumer: Consumer<in SubmittedReportInfo>): Boolean {
        val event = events?.firstOrNull()
        val title = event?.throwableText?.lineSequence()?.first()
            ?: event?.message
            ?: "Crash Report: <Fill in title>"
        val body = event?.throwableText ?: "Please paste the full stacktrace from the IDEA error popup."

        val builder = StringBuilder(URL)
        try {
            builder.append(URLEncoder.encode(title, ENCODING))
            builder.append("&body=")

            builder.append(URLEncoder.encode("### Type of JetBrains IDE (IntelliJ, PyCharm, etc.) and version\n\n", ENCODING))
            builder.append(
                URLEncoder.encode(
                    "### Operating System \n" +
                            "<!-- Windows, Ubuntu, Arch Linux, MacOS, etc. -->\n\n",
                    ENCODING
                )
            )
            builder.append(URLEncoder.encode("### TeXiFy IDEA version\n\n", ENCODING))
            builder.append(URLEncoder.encode("### Description\n", ENCODING))
            builder.append(URLEncoder.encode(additionalInfo ?: "\n", ENCODING))
            builder.append(URLEncoder.encode("\n\n### Stacktrace\n```\n${body.take(7000)}\n```", ENCODING))
        }
        catch (e: UnsupportedEncodingException) {
            consumer.consume(
                SubmittedReportInfo(
                    null,
                    null,
                    SubmittedReportInfo.SubmissionStatus.FAILED
                )
            )
            return false
        }

        BrowserUtil.browse(builder.toString())
        consumer.consume(
            SubmittedReportInfo(
                null,
                "GitHub issue",
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
            )
        )
        return true
    }
}