package nl.hannahsten.texifyidea.action.debug

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.util.Consumer
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.psi.*
import nl.hannahsten.texifyidea.tools.generateSymbolImages
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.files.referencedFileSet
import java.util.regex.Pattern
import javax.swing.JLabel
import javax.swing.SwingConstants

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
