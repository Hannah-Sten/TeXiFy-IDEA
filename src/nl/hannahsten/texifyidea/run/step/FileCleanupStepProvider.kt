package nl.hannahsten.texifyidea.run.step

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.vfs.LocalFileSystem
import nl.hannahsten.texifyidea.util.runWriteAction

// todo file clean up step
object FileCleanupStepProvider {
//    for (originalFile in filesToCleanUp) {
//        val file = LocalFileSystem.getInstance().refreshAndFindFileByPath(originalFile.absolutePath) ?: continue
//        runInEdt {
//            runWriteAction {
//                file.delete(this)
//            }
//        }
//    }
//    filesToCleanUp.clear()
}