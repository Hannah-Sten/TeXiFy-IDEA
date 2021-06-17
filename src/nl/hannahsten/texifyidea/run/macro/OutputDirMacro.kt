package nl.hannahsten.texifyidea.run.macro

import com.intellij.ide.macro.FileDirMacro
import com.intellij.ide.macro.Macro
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext

/**
 * Macro resolving to the output directory of a run config.
 */
class OutputDirMacro : Macro() {

    override fun getName() = "OutputDir"

    val macro = "$$name$"

    override fun getDescription() = "The output directory of this run configuration."

    override fun expand(dataContext: DataContext): String? {
        val virtualFile = OUTPUT_DIR.getData(dataContext)
        return if (virtualFile?.isDirectory == true) {
            getPath(virtualFile)
        }
        else {
            null
        }
    }
}