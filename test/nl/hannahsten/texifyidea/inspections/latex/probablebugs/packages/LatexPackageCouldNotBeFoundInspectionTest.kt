package nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.inspections.latex.probablebugs.packages.LatexPackageCouldNotBeFound
import org.junit.Test

class LatexPackageCouldNotBeFoundInspectionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/packagenotfound"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexPackageCouldNotBeFound())
    }

    @Test
    fun testPackageCouldNotBeFoundWarnings() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(true, false, false, false)
    }
}