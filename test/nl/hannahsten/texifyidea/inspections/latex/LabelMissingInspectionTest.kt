package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import nl.hannahsten.texifyidea.testutils.writeCommand
import org.junit.Test

class GrazieInspectionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testData/inspections/latex"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexMissingLabelInspection())
        (myFixture as? CodeInsightTestFixtureImpl)?.canChangeDocumentDuringHighlighting(true)
    }

    @Test
    fun testMissingLabelWarnings() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(false, false, true, false)
    }

    @Test
    fun testInsertCommandLabelQuickFix() {
        val testName = getTestName(false)
        myFixture.configureByFile("${testName}_before.tex")
        do {
            // we need to collect the fixes again after applying a fix because otherwise
            // the problem descriptors use a cached element from before the applying the fix
            val fix = myFixture.getAllQuickFixes().firstOrNull()
            writeCommand(myFixture.project) {
                fix?.invoke(myFixture.project, myFixture.editor, myFixture.file)
            }
        } while (fix != null)
        myFixture.checkResultByFile("${testName}_after.tex")
    }
}