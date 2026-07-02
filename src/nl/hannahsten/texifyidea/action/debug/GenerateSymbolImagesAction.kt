package nl.hannahsten.texifyidea.action.debug

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import nl.hannahsten.texifyidea.TexifyBundle
import nl.hannahsten.texifyidea.ui.symbols.tools.generateSymbolImages

/**
 * Only for development purposes.
 *
 * @author Hannah Schellekens
 */
@Suppress("ComponentNotRegistered", "unused")
open class GenerateSymbolImagesAction : AnAction(
    TexifyBundle.message("action.generate.symbol.images.text"),
    TexifyBundle.message("action.generate.symbol.images.description"),
    null
) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return

        val fileDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
            .withTitle(TexifyBundle.message("filechooser.symbol.folder.title"))

        FileChooser.chooseFile(fileDescriptor, project, null) {
            generateSymbolImages(it.path)
            Notification(
                "LaTeX",
                TexifyBundle.message("notification.symbol.images.generated.title"),
                TexifyBundle.message("notification.symbol.images.generated.content", it.path),
                NotificationType.INFORMATION
            )
        }
    }
}
