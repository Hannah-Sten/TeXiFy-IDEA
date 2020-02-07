package nl.hannahsten.texifyidea.lang

/**
 * Ignores case: everything will be converted to lower case.
 *
 * @author Lukas Heiligenbrunner
 */
class RequiredFolderArgument(name: String?) : RequiredArgument(name!!, Type.FILE)