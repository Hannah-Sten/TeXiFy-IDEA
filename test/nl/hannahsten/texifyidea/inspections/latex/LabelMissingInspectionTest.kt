package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
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
}