package nl.hannahsten.texifyidea.run.options

import nl.hannahsten.texifyidea.run.macro.MainFileDirMacro

object LatexPathConverterUtil {

    fun toString(value: LatexRunConfigurationAbstractPathOption): String {
        // Remove // to use as separator for the two paths
        return "${value.resolvedPath?.replace("//", "/")}//${value.pathWithMacro}"
    }

    /**
     * @return resolvedPath, pathWithMacro
     */
    fun fromString(value: String): Pair<String?, String?> {
        val splitted = value.split("//", limit = 2)
        var pathWithMacro = splitted.getOrNull(1)
        // It's a magic bug
        if (pathWithMacro == MainFileDirMacro().description) {
            pathWithMacro = "$${MainFileDirMacro().name}$"
        }
        return Pair(splitted.getOrNull(0), pathWithMacro)
    }
}