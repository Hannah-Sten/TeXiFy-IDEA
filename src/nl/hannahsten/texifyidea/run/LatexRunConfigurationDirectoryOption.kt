package nl.hannahsten.texifyidea.run

import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacroManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Component

/**
 * Option for the LaTeX run configuration which is directory-based and thus also macro-based.
 */
open class LatexRunConfigurationDirectoryOption {
    var pathWithMacro: String? = null
        // Can only be set together with resolvedPath
        private set

    var resolvedPath: String? = null
        private set

    /**
     * Attempt to resolve the known path (this is not guaranteed to work, e.g. the file may not exist anymore).
     */
    fun resolve(): VirtualFile? {
        if (resolvedPath?.isBlank() == true) return null
        return LocalFileSystem.getInstance().findFileByPath(resolvedPath ?: return null)
    }

    open fun isDefault() = pathWithMacro == null && resolvedPath == null

    /**
     * @param pathWithMacro String which may contain a Macro.
     * @param resolvedPath Absolute path to a file.
     */
    fun setPath(resolvedPath: String?, pathWithMacro: String? = resolvedPath) {
        // Avoid setting blank paths, as they may resolve to something unwanted (being interpreted as relative path to .../bin)
        if (resolvedPath?.isBlank() == true || pathWithMacro?.isBlank() == true) return
        this.pathWithMacro = pathWithMacro
        this.resolvedPath = resolvedPath?.replace("//", "/") // See below
    }

    /**
     * Expand macros using the data context from the given component and then save it.
     */
    fun resolveAndSetPath(pathWithMacro: String?, component: Component) {
        val context = DataManager.getInstance().getDataContext(component)
        val expandedPath = MacroManager.getInstance().expandMacrosInString(pathWithMacro, true, context)
        setPath(pathWithMacro = pathWithMacro, resolvedPath = expandedPath)
    }

    class Converter : com.intellij.util.xmlb.Converter<LatexRunConfigurationDirectoryOption>() {
        override fun toString(value: LatexRunConfigurationDirectoryOption): String {
            // Assumes that resolvedPath does not contain //
            return "${value.resolvedPath}//${value.pathWithMacro}"
        }

        override fun fromString(value: String): LatexRunConfigurationDirectoryOption {
            val splitted = value.split("//", limit = 2)
            return LatexRunConfigurationDirectoryOption().apply {
                setPath(resolvedPath = splitted.getOrNull(0), pathWithMacro = splitted.getOrNull(1))
            }
        }

    }
}