package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.Magic
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Similar to [ClearAuxFiles].
 */
class ClearGeneratedFiles : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        val files = FileUtils.listFiles(File(project.basePath!!), Magic.File.generatedFileTypes, true)
        files.forEach { it.delete() }
        // todo minted files in _minted-*/*
        // todo dialog
        LocalFileSystem.getInstance().refresh(true)
    }
}