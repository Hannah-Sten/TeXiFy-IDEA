package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.inspections.latex.probablebugs.LatexLabelBeforeCaptionInspection
import nl.hannahsten.texifyidea.testutils.writeCommand
import org.junit.Test

class LatexLabelBeforeCaptionInspectionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/labelaftercaption"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexLabelBeforeCaptionInspection(), LatexMissingLabelInspection())
    }

    @Test
    fun testLabelAfterCaptionWarnings() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(false, false, true, false)
    }

    @Test
    fun testSwapLabelCaption() {
        val testName = getTestName(false)
        myFixture.configureByFile("${testName}_before.tex")
        do {
            // we need to collect the fixes again after applying a fix because otherwise
            // the problem descriptors use a cached element from before the applying the fix
            val allQuickFixes = myFixture.getAllQuickFixes()
            val fix = allQuickFixes.firstOrNull()
            writeCommand(myFixture.project) {
                fix?.invoke(myFixture.project, myFixture.editor, myFixture.file)
            }
        } while (fix != null)
        myFixture.checkResultByFile("${testName}_after.tex")
    }
}