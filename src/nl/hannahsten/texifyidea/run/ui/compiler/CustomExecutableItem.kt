package nl.hannahsten.texifyidea.run.ui.compiler

import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.PathUtil
import nl.hannahsten.texifyidea.run.executable.CustomExecutable

/**
 * executable selector item that was created by the user (through first selecting [AddExecutableItem]).
 *
 * @author Sten Wessel
 */
class CustomExecutableItem(val executable: CustomExecutable) : ExecutableComboBoxItem {

    override val presentableText = FileUtil.toSystemIndependentName(executable.executablePath)
    override val command = PathUtil.getFileName(presentableText)

    override val order = 1
}
