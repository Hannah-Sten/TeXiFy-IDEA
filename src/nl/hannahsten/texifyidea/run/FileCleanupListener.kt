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
 */
class FileCleanupListener(private val filesToCleanUp: MutableList<File>) : ProcessListener {

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
    }

    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
    }
}