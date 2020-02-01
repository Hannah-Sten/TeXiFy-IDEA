package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.testutils.writeCommand
import org.junit.Test

class UseOfVerbatimInspectionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/verbatim"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexVerbatimInspection())
    }

    @Test
    fun testUseOfVerbatim() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(false, false, true, false)
    }

    @Test
    fun testInsertFormatterDisablingComments() {
        val testName = getTestName(false)
        myFixture.configureByFile("${testName}_before.tex")
        val allQuickFixes = myFixture.getAllQuickFixes()
        val fix = allQuickFixes.firstOrNull { it.familyName.startsWith("Insert") }
        writeCommand(myFixture.project) {
            fix?.invoke(myFixture.project, myFixture.editor, myFixture.file)
        }
        myFixture.checkResultByFile("${testName}_after.tex")
    }
}