package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.Magic
import java.io.File

/**
 * Similar to [ClearAuxFiles].
 */
class ClearGeneratedFiles : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        File(project.basePath!!).walk().filter { it.isFile }
                .filter { it.extension in Magic.File.generatedFileTypes }
                .forEach { it.delete() }
        // todo minted files in _minted-*/*
        // todo dialog
        LocalFileSystem.getInstance().refresh(true)
    }
}