package nl.hannahsten.texifyidea.report

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.protocol.SentryId

/**
 * Taken from https://github.com/FirstTimeInForever/intellij-pdf-viewer/blob/1fa294eeef15f977d8d09cb65b69fc19bc53aec9/plugin/src/main/kotlin/com/firsttimeinforever/intellij/pdf/viewer/report/SendReportBackgroundTask.kt#L17
 */
internal class SendReportBackgroundTask(
    project: Project?,
    private val events: List<SentryEvent>,
    private val consumer: Consumer<in SubmittedReportInfo>
) : Task.Backgroundable(project, "Sending error report") {
    override fun run(indicator: ProgressIndicator) {
        for (event in events) {
            val id: SentryId = Sentry.captureEvent(event)
            if (id != SentryId.EMPTY_ID) {
                ApplicationManager.getApplication().invokeLater {
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE))
                }
            } else {
                ApplicationManager.getApplication().invokeLater {
                    val group = NotificationGroupManager.getInstance().getNotificationGroup("Error Report")
                    group.createNotification(
                        "Failed to submit error report!",
                        NotificationType.ERROR
                    ).notify(project)
                    thisLogger().error(event.toString())
                    consumer.consume(SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.FAILED))
                }
            }
        }
    }
}