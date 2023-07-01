package nl.hannahsten.texifyidea

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
 * Represents a signal that a request to one of the remote libraries failed.
 */
data class RemoteLibraryRequestFailure(val libraryName: String, val response: HttpResponse)

/**
 * A system command/program that failed to run successfully.
 */
data class CommandFailure(val output: String, val exitCode: Int)
