package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.editor.FileDropEvent
import com.intellij.openapi.editor.FileDropHandler
import nl.hannahsten.texifyidea.action.wizard.graphic.InsertGraphicWizardAction
import nl.hannahsten.texifyidea.util.files.isLatexFile
import nl.hannahsten.texifyidea.util.files.psiFile
import nl.hannahsten.texifyidea.util.magic.FileMagic
import java.io.File
import java.util.*

/**
 * @author Hannah Schellekens
 */
open class GraphicsDragAndDropHandler : FileDropHandler {

    /**
     * Whether the file can be dropped using this handler.
     */
    private fun File.isDroppable(): Boolean {
        return extension.lowercase(Locale.getDefault()) in FileMagic.graphicFileExtensions
    }


    override suspend fun handleDrop(e: FileDropEvent): Boolean {
        // Only allow dropping in LaTeX sources.
        val editor = e.editor ?: return false
        if (editor.document.psiFile(e.project)?.isLatexFile() == false) return false
        // To be safe, don't trigger if there is a mix of files
        if (e.files.any { !it.isDroppable() }) return false

        for (toDrop in e.files.filter { it.isDroppable() }) {
            editor.document.psiFile(e.project)?.virtualFile?.let { file ->
                InsertGraphicWizardAction(toDrop).executeAction(file, e.project)
            }
        }

        return true
    }
}