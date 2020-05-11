package nl.hannahsten.texifyidea.lang

/**
 * @author Lukas Heiligenbrunner
 */
// We have to explicitly override isAbsolutePathSupported to avoid a NoSuchMethodError
class RequiredPicturePathArgument(name: String, override val isAbsolutePathSupported: Boolean = true, vararg extension: String) : RequiredFileArgument(name, isAbsolutePathSupported, *extension)