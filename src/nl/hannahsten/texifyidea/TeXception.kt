package nl.hannahsten.texifyidea

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import io.ktor.client.statement.*

/**
 * Exception that is thrown by problems within TeXiFy-IDEA.
 * Consider using PluginException if the exception is not caught within TeXiFy, otherwise the real exception message will be hidden.
 *
 * @author Hannah Schellekens
 */
open class TeXception : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

/**
 * Exceptian that is thrown to signal that a request to one of the remote libraries failed.
 */
class RemoteLibraryRequestTeXception(private val libraryName: String, private val response: HttpResponse) : TeXception() {

    /**
     * Shows a notification with information about the failed request.
     */
    fun showNotification(project: Project) {
        val title = "Could not connect to $libraryName"
        val statusMessage = "${response.status.value}: ${response.status.description}"
        Notification("LaTeX", title, statusMessage, NotificationType.ERROR).notify(project)
    }
}