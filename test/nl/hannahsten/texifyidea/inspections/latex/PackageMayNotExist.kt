package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class PackageMayNotExist : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/nonexistent-package"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexPackageMayNotExistInspection())
    }

    @Test
    fun testPackageMayNotExistWarnings() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(true, false, false, false)
    }
}