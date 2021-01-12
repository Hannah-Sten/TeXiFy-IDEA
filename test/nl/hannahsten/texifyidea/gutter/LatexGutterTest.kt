package nl.hannahsten.texifyidea.gutter

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LatexGutterTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/gutter"
    }

    @Test
    fun testShowCompileGutter() {
        val testName = getTestName(false)
        val gutters = myFixture.findAllGutters("$testName.tex")
        assertEquals(1, gutters.size)
        assertEquals("Compile document", gutters.first().tooltipText)
    }

    @Test
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

    @Test
    fun testShowNavigationGutter() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.doHighlighting()
        val gutters = myFixture.findAllGutters()
        assertEquals(5, gutters.size)
        assertTrue(gutters.all { g -> g.tooltipText == "Go to referenced file" })
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