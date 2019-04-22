package nl.rubensten.texifyidea.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.io.endsWithName
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.project.isDirectoryBased
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import nl.rubensten.texifyidea.util.Magic
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.RegexFileFilter
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter
import java.nio.file.Files

/**
 * @author Abby Berkers
 *
 * Action to delete all auxiliary files.
 */
class ClearAuxFiles : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return
        val files = FileUtils.listFiles(File(project.basePath), Magic.File.auxiliaryFileTypes, true)
        files.forEach { it.delete() }
        LocalFileSystem.getInstance().refresh(true)
    }
}