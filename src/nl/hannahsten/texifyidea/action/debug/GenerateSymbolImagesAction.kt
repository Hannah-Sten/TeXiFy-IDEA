package nl.hannahsten.texifyidea.action.debug

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import nl.hannahsten.texifyidea.ui.symbols.tools.generateSymbolImages
import nl.hannahsten.texifyidea.util.toastInfo

/**
 * @author Hannah Schellekens
 */
open class GenerateSymbolImagesAction : AnAction(
        "Generate Symbol Images",
        "(Development) Generates the symbol images for the symbol view",
        null
) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return

        val fileDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
                .withTitle("Select symbols folder...")

        FileChooser.chooseFile(fileDescriptor, project, null) {
            generateSymbolImages(it.path)
            toastInfo(project, "<html>Generated symbol images to:<br>${it.path}</html>")
        }
    }
}
