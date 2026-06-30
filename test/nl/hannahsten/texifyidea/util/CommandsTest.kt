package nl.hannahsten.texifyidea.util

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files

class CommandsTest : BasePlatformTestCase() {

    fun testExpandGlobRecursivelyFromCurrentRootDir() {
        val root = Files.createTempDirectory("texify-expand-glob")
        Files.createDirectories(root.resolve("bib"))
        Files.createDirectories(root.resolve("bib/nested"))
        Files.writeString(root.resolve("root.bib"), "root")
        Files.writeString(root.resolve("bib/second.bib"), "second")
        Files.writeString(root.resolve("bib/secone.bib"), "second")
        Files.writeString(root.resolve("bib/nested/third.bib"), "third")
        Files.writeString(root.resolve("bib/nested/ignored.txt"), "ignored")

        val rootVirtualFile = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByPath(root.toString()))

        val matches = expandGlob("bib/*.bib", rootVirtualFile)

        assertEquals(listOf("bib/second.bib", "bib/secone.bib"), matches)

        assertEquals(listOf("bib/second.bib"), expandGlob("bib/se?ond.bib", rootVirtualFile))
        assertEquals(listOf("bib/second.bib", "bib/secone.bib"), expandGlob("bib/secon[de].bib", rootVirtualFile))
        assertEquals(listOf("bib/second.bib"), expandGlob("bib/secon[!e].bib", rootVirtualFile))
    }
}
