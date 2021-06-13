package nl.hannahsten.texifyidea.run.options

import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacroManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Component

/**
 * See [LatexRunConfigurationPathOption], but using an abstract class because we cannot subclass data classes.
 */
abstract class LatexRunConfigurationAbstractPathOption(open val pathWithMacro: String? = null, open val resolvedPath: String? = null) {

    companion object {
        /**
         * Expand macros using the data context from the given component and then save it.
         *
         * @param factory: Create option based on resolvedPath and pathWithMacro (in that order).
         */
        fun <O : LatexRunConfigurationAbstractPathOption> resolveAndGetPath(
            pathWithMacro: String?,
            component: Component,
            factory: (String?, String?) -> O
        ): O {
            val context = DataManager.getInstance().getDataContext(component)
            val expandedPath = MacroManager.getInstance().expandMacrosInString(pathWithMacro, true, context)
            return factory(expandedPath, pathWithMacro)
        }
    }

    /**
     * Attempt to resolve the known path (this is not guaranteed to work, e.g. the file may not exist anymore).
     */
    fun resolve(): VirtualFile? {
        if (resolvedPath?.isBlank() == true) return null
        return LocalFileSystem.getInstance().findFileByPath(resolvedPath ?: return null)
    }

    open fun isDefault() = pathWithMacro == null && resolvedPath == null

    class Converter : com.intellij.util.xmlb.Converter<LatexRunConfigurationAbstractPathOption>() {
        override fun toString(value: LatexRunConfigurationAbstractPathOption): String {
            return LatexPathConverterUtil.toString(value)
        }

        override fun fromString(value: String): LatexRunConfigurationAbstractPathOption {
            val (resolvedPath, pathWithMacro) = LatexPathConverterUtil.fromString(value)
            return LatexRunConfigurationPathOption(resolvedPath, pathWithMacro)
        }
    }
}