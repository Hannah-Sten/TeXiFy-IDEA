package nl.hannahsten.texifyidea.run.options

import com.intellij.openapi.vfs.VirtualFile

/**
 * Option for the LaTeX run configuration which is directory-based and thus also macro-based.
 * It has to be immutable, otherwise when cloning a run configuration, the option will just be copied i.e. point to the same mutable class instance.
 */
data class LatexRunConfigurationPathOption(override val resolvedPath: String? = null, override val pathWithMacro: String? = resolvedPath) : LatexRunConfigurationAbstractPathOption(pathWithMacro, resolvedPath) {

    companion object {
        fun createDefaultWorkingDirectory(mainFile: VirtualFile) = LatexRunConfigurationPathOption(mainFile.parent.path, defaultWorkingDirectoryWithMacro)
    }
}
