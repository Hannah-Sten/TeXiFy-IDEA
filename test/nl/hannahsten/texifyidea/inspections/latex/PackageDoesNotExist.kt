package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class PackageDoesNotExist : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/nonexistent-package"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexVerbatimInspection())
    }

    @Test
    fun testPackageDoesNotExistWarnings() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(true, false, false, false)
    }
}