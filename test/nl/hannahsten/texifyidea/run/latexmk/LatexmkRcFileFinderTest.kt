package nl.hannahsten.texifyidea.run.latexmk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.util.LatexmkRcFileFinder
import java.nio.file.Files

class LatexmkRcFileFinderTest : BasePlatformTestCase() {

    fun testHasLatexmkRcFindsLocalRcInWorkingDirectory() {
        val tempDir = Files.createTempDirectory("texify-latexmkrc")
        Files.createFile(tempDir.resolve(".latexmkrc"))

        val hasRc = LatexmkRcFileFinder.hasLatexmkRc(compilerArguments = null, workingDirectory = tempDir)
        assertTrue(hasRc)
    }

    fun testHasLatexmkRcReturnsFalseWithoutRc() {
        val tempDir = Files.createTempDirectory("texify-latexmkrc-empty")

        val hasRc = LatexmkRcFileFinder.hasLatexmkRc(compilerArguments = null, workingDirectory = tempDir)
        assertFalse(hasRc)
    }
}
