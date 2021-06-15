package nl.hannahsten.texifyidea.run.macro

import com.intellij.ide.macro.FileDirMacro
import com.intellij.ide.macro.Macro
import com.intellij.openapi.actionSystem.DataContext

/**
 * Macro for in run configuration textfields.
 */
class MainFileDirMacro : Macro() {

    override fun getName() = "MainFileDir"

    val macro = "$$name$"

    override fun getDescription() = "The directory which contains the main LaTeX file"

    override fun expand(dataContext: DataContext): String? {
        return FileDirMacro().expand(dataContext)
    }
}