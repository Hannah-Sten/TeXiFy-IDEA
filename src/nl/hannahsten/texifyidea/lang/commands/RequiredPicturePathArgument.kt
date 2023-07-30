package nl.hannahsten.texifyidea.lang.commands

/**
 * @author Lukas Heiligenbrunner
 */
// We have to explicitly override isAbsolutePathSupported to avoid a NoSuchMethodError
class RequiredPicturePathArgument(name: String, override val isAbsolutePathSupported: Boolean = true, override val commaSeparatesArguments: Boolean = true, extension: List<String>) : RequiredFileArgument(name, isAbsolutePathSupported, commaSeparatesArguments, *extension.toTypedArray())