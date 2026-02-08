package nl.hannahsten.texifyidea.action.debug

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import nl.hannahsten.texifyidea.ui.symbols.tools.generateSymbolImages

/**
 * Only for development purposes.
 *
 * @author Hannah Schellekens
 */
@Suppress("ComponentNotRegistered")
open class GenerateSymbolImagesAction : AnAction(
    "Generate Symbol Images",
    "(Development) Generates the symbol images for the symbol view",
    null
) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return

        val fileDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
            .withTitle("Select Symbols Folder...")

        FileChooser.chooseFile(fileDescriptor, project, null) {
            generateSymbolImages(it.path)
            Notification("LaTeX", "Finished generating symbol images", "<html>Generated symbol images to:<br>${it.path}</html>", NotificationType.INFORMATION)
        }
    }
}
