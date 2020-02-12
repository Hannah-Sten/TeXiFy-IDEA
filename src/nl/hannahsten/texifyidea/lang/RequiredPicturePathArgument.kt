package nl.hannahsten.texifyidea.lang

/**
 * @author Lukas Heiligenbrunner
 */
class RequiredPicturePathArgument(name : String, vararg extension: String) : RequiredFileArgument(name, *extension)