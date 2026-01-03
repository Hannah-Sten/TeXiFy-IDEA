package nl.hannahsten.texifyidea

/**
 * Exception that is thrown by problems within TeXiFy-IDEA.
 * Consider using PluginException if the exception is not caught within TeXiFy, otherwise the real exception message will be hidden.
 *
 * @author Hannah Schellekens
 */
open class TeXception : RuntimeException {

    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Represents a signal that a request to one of the remote libraries failed.
 */
data class RemoteLibraryRequestFailure(val libraryName: String, val message: String)

/**
 * A system command/program that failed to run successfully.
 */
data class CommandFailure(val output: String, val exitCode: Int)