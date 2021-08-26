package nl.hannahsten.texifyidea

/**
 * Exception that is thrown by problems within TeXiFy-IDEA.
 * Consider using PluginException if the exception is not caught within TeXiFy, otherwise the real exception message will be hidden.
 *
 * @author Hannah Schellekens
 */
class TeXception : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}