package nl.hannahsten.texifyidea.run.latex.step

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.run.latex.FileCleanupStepOptions
import nl.hannahsten.texifyidea.util.runWriteAction
import kotlin.io.path.absolutePathString

internal class FileCleanupRunStep(
    stepConfig: FileCleanupStepOptions,
) : InlineLatexRunStep {

    override val configId: String = stepConfig.id
    override val id: String = stepConfig.type

    override fun runInline(context: LatexRunStepContext): Int {
        val state = context.session
        val filesToDelete = state.filesToCleanUp.toList()

        return try {
            deleteFilesViaVfs(filesToDelete)
            0
        }
        finally {
            state.filesToCleanUp.clear()
        }
    }

    private fun deleteFilesViaVfs(files: List<java.nio.file.Path>) {
        if (files.isEmpty()) {
            return
        }

        val deleteAction = {
            val fileSystem = LocalFileSystem.getInstance()
            files.forEach { path ->
                val file = fileSystem.refreshAndFindFileByPath(path.absolutePathString()) ?: return@forEach
                runCatching {
                    file.delete(this)
                }
            }
        }

        val application = ApplicationManager.getApplication()
        if (application.isDispatchThread) {
            runWriteAction(deleteAction)
        }
        else {
            application.invokeAndWait {
                runWriteAction(deleteAction)
            }
        }
    }
}
