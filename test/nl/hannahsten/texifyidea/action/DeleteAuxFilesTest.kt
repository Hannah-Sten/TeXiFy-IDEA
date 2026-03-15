package nl.hannahsten.texifyidea.action

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path

class DeleteAuxFilesTest : BasePlatformTestCase() {

    fun testDeleteAuxFilesRemovesTemporaryBuildFilesProjectWide() {
        val projectRoot = Path.of(project.basePath!!)
        val nestedDir = Files.createDirectories(projectRoot.resolve("nested/deeper"))
        val auxFile = projectRoot.resolve("main.aux")
        val logFile = nestedDir.resolve("chapter.log")
        val synctexFile = nestedDir.resolve("chapter.synctex")
        val pdfFile = projectRoot.resolve("main.pdf")
        val xdvFile = nestedDir.resolve("chapter.xdv")
        Files.writeString(auxFile, "aux")
        Files.writeString(logFile, "log")
        Files.writeString(synctexFile, "synctex")
        Files.writeString(pdfFile, "pdf")
        Files.writeString(xdvFile, "xdv")

        DeleteAuxFiles().actionPerformed(createProjectEvent())

        assertFalse(Files.exists(auxFile))
        assertFalse(Files.exists(logFile))
        assertFalse(Files.exists(synctexFile))
        assertFalse(Files.exists(xdvFile))
        assertTrue(Files.exists(pdfFile))
    }

    private fun createProjectEvent(): AnActionEvent = AnActionEvent(
        SimpleDataContext.getProjectContext(project),
        Presentation(),
        ActionPlaces.UNKNOWN,
        ActionUiKind.NONE,
        null,
        0,
        ActionManager.getInstance(),
    )
}
