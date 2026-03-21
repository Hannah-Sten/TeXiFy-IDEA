package nl.hannahsten.texifyidea.util.files

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Path

class LatexTemporaryBuildArtifactsTest : BasePlatformTestCase() {

    fun testMatchesTemporaryBuildArtifacts() {
        assertTrue(LatexTemporaryBuildArtifacts.matches("main.log"))
        assertTrue(LatexTemporaryBuildArtifacts.matches("main.synctex"))
        assertTrue(LatexTemporaryBuildArtifacts.matches("main.synctex.gz"))
        assertTrue(LatexTemporaryBuildArtifacts.matches("main.synctex(busy)"))
        assertTrue(LatexTemporaryBuildArtifacts.matches("main.xdv"))
        assertFalse(LatexTemporaryBuildArtifacts.matches("main.pdf"))
    }

    fun testMatchesMainDocumentArtifactUsesBaseName() {
        assertTrue(LatexTemporaryBuildArtifacts.matchesMainDocumentArtifact(Path.of("main.aux"), "main"))
        assertTrue(LatexTemporaryBuildArtifacts.matchesMainDocumentArtifact(Path.of("main.synctex"), "main"))
        assertTrue(LatexTemporaryBuildArtifacts.matchesMainDocumentArtifact(Path.of("MAIN.XDV"), "main"))
        assertFalse(LatexTemporaryBuildArtifacts.matchesMainDocumentArtifact(Path.of("other.aux"), "main"))
        assertFalse(LatexTemporaryBuildArtifacts.matchesMainDocumentArtifact(Path.of("chapter1.log"), "main"))
    }

    fun testIgnoredFileMasksAreDerivedFromExtensionsAndSuffixes() {
        assertEquals("*.aux", LatexTemporaryBuildArtifacts.ignoredFileMasks.first())
        assertTrue("*.synctex" in LatexTemporaryBuildArtifacts.ignoredFileMasks)
        assertTrue("*.synctex.gz" in LatexTemporaryBuildArtifacts.ignoredFileMasks)
        assertTrue("*.synctex(busy)" in LatexTemporaryBuildArtifacts.ignoredFileMasks)
        assertTrue("*.xdv" in LatexTemporaryBuildArtifacts.ignoredFileMasks)
    }
}
