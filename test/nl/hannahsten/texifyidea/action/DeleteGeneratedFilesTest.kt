package nl.hannahsten.texifyidea.action

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path

class DeleteGeneratedFilesTest : BasePlatformTestCase() {

    fun testDeleteGeneratedFilesKeepsBroaderCleanupBehavior() {
        val projectRoot = Path.of(project.basePath!!)
        val srcDir = Files.createDirectories(projectRoot.resolve("src"))
        val auxDir = Files.createDirectories(projectRoot.resolve("auxil"))
        val outDir = Files.createDirectories(projectRoot.resolve("out"))
        val tempLog = srcDir.resolve("main.log")
        val generatedPdf = srcDir.resolve("main.pdf")
        val generatedXdv = srcDir.resolve("main.xdv")
        val keepSource = srcDir.resolve("main.tex")
        val mintedDir = Files.createDirectories(srcDir.resolve("_minted-main"))
        val mintedFile = mintedDir.resolve("cache.pyg")
        val auxOutput = auxDir.resolve("main.aux")
        val outOutput = outDir.resolve("main.toc")
        Files.writeString(tempLog, "log")
        Files.writeString(generatedPdf, "pdf")
        Files.writeString(generatedXdv, "xdv")
        Files.writeString(keepSource, "\\\\documentclass{article}")
        Files.writeString(mintedFile, "minted")
        Files.writeString(auxOutput, "aux")
        Files.writeString(outOutput, "toc")

        val notDeleted = DeleteGeneratedFilesSupport.deleteProjectGeneratedFiles(
            basePath = projectRoot,
            customOutput = emptyList(),
        )

        assertTrue(notDeleted.isEmpty())
        assertFalse(Files.exists(tempLog))
        assertFalse(Files.exists(generatedPdf))
        assertFalse(Files.exists(generatedXdv))
        assertFalse(Files.exists(mintedFile))
        assertFalse(Files.exists(auxOutput))
        assertFalse(Files.exists(outOutput))
        assertTrue(Files.exists(keepSource))
        assertFalse(Files.exists(mintedDir))
    }
}
