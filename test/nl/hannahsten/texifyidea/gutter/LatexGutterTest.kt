package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.TexifyIcons
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateFilesets
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexGutterTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    override fun getTestDataPath(): String {
        return "test/resources/gutter"
    }

    fun testPackageGutter() {
        for (i in 1..3) {
            myFixture.addFileToProject(
                "amsmath$i.sty",
                """
                \ProvidesPackage{amsmath$i}
                """.trimIndent()
            )
        }
        myFixture.configureByText(
            "main.tex",
            """
            \usepackage{amsmath1,amsmath2,amsmath3}
            """.trimIndent()
        )
        myFixture.updateFilesets()
        val gutters = myFixture.findAllGutters()
        assertEquals(1, gutters.size) // only one gutter for the \usepackage command
    }

    fun testShowCompileGutter() {
        val testName = getTestName(false)
        val gutters = myFixture.findAllGutters("$testName.tex")
        assertEquals(1, gutters.size)
        assertEquals("Compile document", gutters.first().tooltipText)
    }

    fun testGraphicsExtensions() {
        val testName = getTestName(false)
        myFixture.copyDirectoryToProject("figures", "figures")
        val gutters = myFixture.findAllGutters("$testName.tex")
        // The last one is \includegraphics{figures/duck}, which is a pdf file.
        assertEquals(TexifyIcons.PDF_FILE, gutters.last().icon)
    }

    fun testShowMethodSeparators() {
        val testName = getTestName(false)
        withLineMarkersEnabled {
            myFixture.configureByFile("$testName.tex")
            myFixture.doHighlighting()
            val lineMarkers = DaemonCodeAnalyzerImpl
                .getLineMarkers(myFixture.editor.document, myFixture.project)
                .filter { l -> l.separatorPlacement != null }
            assertTrue(lineMarkers.any { l -> getLineMarkerLine(l) == 1 })
            assertTrue(lineMarkers.any { l -> getLineMarkerLine(l) == 4 })
        }
    }

    fun testShowNavigationGutter() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.doHighlighting()
        val gutters = myFixture.findAllGutters()
        assertEquals(5, gutters.size)
        assertTrue(gutters.all { g -> g.tooltipText == "Go to referenced file" })
    }

    fun testInfiniteColorLoop() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{xcolor}
            \colorlet{kameel}{oliefant}
            \colorlet{oliefant}{kameel}
            """.trimIndent()
        )
        myFixture.findAllGutters()
    }

    private fun getLineMarkerLine(marker: LineMarkerInfo<*>): Int {
        return myFixture.editor.document.getLineNumber((marker.element as LeafPsiElement).textRange.startOffset)
    }

    private fun withLineMarkersEnabled(action: () -> Unit) {
        val before = DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS
        DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS = true
        try {
            action()
        }
        finally {
            DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS = before
        }
    }
}