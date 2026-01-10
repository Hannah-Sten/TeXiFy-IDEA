package nl.hannahsten.texifyidea.report

import arrow.resilience.Schedule
import arrow.resilience.retry
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.intellij.ide.DataManager
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.idea.IdeaLogger
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.Consumer
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import kotlinx.coroutines.runBlocking
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import java.awt.Component
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Send error report to Sentry.
 * Previously, we used to submit to GitHub, but it resulted in too much spam and duplicates.
 * For the original implementation, see https://github.com/Hannah-Sten/TeXiFy-IDEA/blob/2cf0b6a8dbdbeb2355249f80a00b9082bc64c6a5/src/nl/hannahsten/texifyidea/LatexErrorReportSubmitter.kt
 * Also see the PdfErrorReportSubmitter in the PDF Viewer plugin.
 *
 * @author Sten Wessel
 */
class LatexErrorReportSubmitter : ErrorReportSubmitter() {

    override fun getReportActionText() = "Report to TeXiFy-IDEA"

    override fun submit(
        events: Array<out IdeaLoggingEvent>,
        additionalInfo: String?,
        parentComponent: Component,
        consumer: Consumer<in SubmittedReportInfo>
    ): Boolean {
        val retrySchedule = Schedule.exponential<Throwable>(250.milliseconds)

        val latestVersion = try {
            runBlocking {
                retrySchedule.retry { Util.getLatestVersion() }
            }
        }
        // Don't do the check when there's no internet connection
        catch (e: UnknownHostException) {
            return true
        }

        if (latestVersion.version.toString().isNotBlank() && DefaultArtifactVersion(Util.currentVersion) < latestVersion.version) {
            val currentIdeaVersion =
                DefaultArtifactVersion(ApplicationInfo.getInstance().build.asStringWithoutProductCode())
            val requiredIdeaVersion = latestVersion.ideaVersion.sinceBuild

            JBPopupFactory.getInstance().createMessage("")
                .showInCenterOf(parentComponent)

            val message = "Please update your current version (${Util.currentVersion}) of TeXiFy to the latest version (${latestVersion.version}) before submitting,\n" +
                "to check if the error is already fixed. Go to Settings > Plugins to update.\n" +
                if (currentIdeaVersion < requiredIdeaVersion) "You first need to update your current IDE version ($currentIdeaVersion) to $requiredIdeaVersion or newer.\n" else ""

            val result = MessageDialogBuilder.okCancel("Update TeXiFy", message)
                .yesText("Cancel Submit") // Sort of the wrong way around, but it suggests to cancel this way
                .noText("Submit Anyway")
                .ask(parentComponent)

            // If the user cancels, don't submit
            return !result
        }

        return submitEvents(events, additionalInfo, parentComponent, consumer)
    }

    private fun submitEvents(events: Array<out IdeaLoggingEvent>?, additionalInfo: String?, parentComponent: Component, consumer: Consumer<in SubmittedReportInfo>): Boolean {
        Sentry.init { options ->
            // PHPirates' Sentry project
            options.dsn = "https://b238e72aa4e237556b9476465dea9116@o4508981186461696.ingest.de.sentry.io/4510618514882640"
        }

        val context = DataManager.getInstance().getDataContext(parentComponent)
        val sentryEvents = createEvents(events ?: return false, additionalInfo)
        val project = CommonDataKeys.PROJECT.getData(context)

        SendReportBackgroundTask(project, sentryEvents, consumer).queue()

        return true
    }

    private fun createEvents(events: Array<out IdeaLoggingEvent>, additionalInfo: String?): List<SentryEvent> = events
        .map { ideaEvent ->
            SentryEvent().apply {
                this.message = Message().apply { this.message = additionalInfo ?: ideaEvent.throwableText }
                this.level = SentryLevel.ERROR
                this.throwable = ideaEvent.throwable

                (pluginDescriptor as? IdeaPluginDescriptor)?.let { release = it.version }
                extras = mapOf("last_action" to IdeaLogger.ourLastActionId)
                val applicationNamesInfo = ApplicationNamesInfo.getInstance()
                val instanceEx = ApplicationInfoEx.getInstanceEx()
                tags = mapOf(
                    "os_name" to SystemInfo.OS_NAME,
                    "os_version" to SystemInfo.OS_VERSION,
                    "os_arch" to SystemInfo.OS_ARCH,
                    "plugin_version" to Util.currentVersion,
                    "app_name" to applicationNamesInfo.productName,
                    "app_full_name" to applicationNamesInfo.fullProductName,
                    "app_version_name" to instanceEx.versionName,
                    "is_eap" to instanceEx.isEAP.toString(),
                    "app_build" to instanceEx.build.asString(),
                    "app_version" to instanceEx.fullVersion,
                )
            }
        }

    object Util {
        private var latestVersionCached = IdeaPlugin()
        fun getLatestVersion(): IdeaPlugin {
            if (latestVersionCached.version.toString().isNotBlank()) return latestVersionCached

            // Create xml mapper that doesn't fail on unknown properties. This allows us to only define the properties we need.
            val mapper = XmlMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            val connection = (URI.create(JETBRAINS_API_URL).toURL().openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1000
                readTimeout = 1000
            }

            val inputString: String = try {
                connection.inputStream.reader().use { it.readText() }
            }
            catch (e: SocketTimeoutException) {
                return latestVersionCached
            }
            latestVersionCached = mapper.readValue(inputString, PluginRepo::class.java)
                .category
                ?.maxByOrNull { it.version }
                ?: latestVersionCached

            return latestVersionCached
        }

        val currentVersion by lazy {
            PluginManagerCore.getPlugin(PluginId.getId("nl.rubensten.texifyidea"))?.version
        }

        /**
         * If the stacktrace is too long, collect lines referring to TeXiFy only.
         */
        fun filterInterestingLines(body: String): String {
            val lines = body.split("\n").filter { it.isNotBlank() }
            val texifyLines = lines.mapIndexedNotNull { i: Int, line: String -> if ("nl.hannahsten.texifyidea" in line || "Caused by:" in line) i else null }
            val interestingLines = ((0..10).toSet() + texifyLines.flatMap { listOf(it - 1, it, it + 1) }.toSet()).toList().sorted()
            return interestingLines.foldIndexed("") { i, stacktrace, lineIndex ->
                stacktrace + (if (i > 0 && interestingLines[i - 1] < lineIndex - 1) "\n        (...)" else "") + "\n" + lines.getOrElse(lineIndex) { "" }.take(500)
            }.trim()
        }
    }

    companion object {

        private const val JETBRAINS_API_URL = "https://plugins.jetbrains.com/plugins/list?pluginId=9473"
    }

    /**
     * Plugin repo data class which is the root of the xml response from the [Marketplace API](https://plugins.jetbrains.com/docs/marketplace/api-reference.html).
     */
    data class PluginRepo(
        /**
         * Wrapper for multiple idea-plugin objects. Has to be nullable AND have default value to avoid exception.
         */
        @JacksonXmlProperty(localName = "idea-plugin")
        @JacksonXmlElementWrapper(useWrapping = false)
        val category: List<IdeaPlugin>? = emptyList()
    )

    /**
     * Data class specifying all the properties we need from the idea-plugin tag from the xml response.
     * All properties have to have a default value, because the class needs to have an empty constructor.
     */
    data class IdeaPlugin(
        @JacksonXmlProperty(localName = "version")
        val version: DefaultArtifactVersion = DefaultArtifactVersion(""),
        @JacksonXmlProperty(localName = "idea-version")
        val ideaVersion: IdeaVersion = IdeaVersion()
    )

    data class IdeaVersion(
        @JacksonXmlProperty(isAttribute = true, localName = "since-build")
        val sinceBuild: DefaultArtifactVersion = DefaultArtifactVersion("")
    )
}