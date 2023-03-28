package nl.hannahsten.texifyidea.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.runWriteAction
import java.io.File

/**
 * Clean up given files after the process is done.
 *
 * @param filesToCleanUp Delete these files.
 * @param filesToCleanUpIfEmpty Delete these files only if they are an empty directory.
 */
class FileCleanupListener(private val filesToCleanUp: MutableList<File>, private val filesToCleanUpIfEmpty: Set<File> = setOf()) : ProcessListener {

    override fun startNotified(event: ProcessEvent) {
    }

    override fun processTerminated(event: ProcessEvent) {
        for (originalFile in filesToCleanUp) {
            val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(originalFile.absolutePath) ?: continue
            runInEdt {
                runWriteAction {
                    file.delete(this)
                }
            }
        }
        filesToCleanUp.clear()

        // Make sure to delete children first, so that we also delete directories containing only directories
        for (file in filesToCleanUpIfEmpty.sortedDescending()) {
            if (file.isDirectory && file.list()?.isEmpty() == true) {
                file.delete()
            }
        }
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
    }
}