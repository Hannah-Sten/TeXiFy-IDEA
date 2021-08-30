package nl.hannahsten.texifyidea.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LatexFoldingTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/editor/folding"
    }

    fun testDashFolding() {
        myFixture.configureByFile("dash.tex")
        myFixture.testFoldingWithCollapseStatus("$testDataPath/dash.tex")
    }
}