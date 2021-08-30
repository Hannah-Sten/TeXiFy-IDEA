package nl.hannahsten.texifyidea.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Note that folding builders need to implement DumbAware.
 */
class LatexFoldingTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/editor/folding"
    }

    fun testDashFolding() {
        myFixture.testFoldingWithCollapseStatus("$testDataPath/dash.tex")
    }

    fun testEnvironmentFolding() {
        myFixture.testFolding("$testDataPath/environment.tex")
    }

    fun testEscapedSymbolFolding() {
        myFixture.testFolding("$testDataPath/escaped-symbols.tex")
    }

    fun testMathSymbolFolding() {
        myFixture.testFolding("$testDataPath/math-symbols.tex")
    }

    fun testSectionFolding() {
        myFixture.testFolding("$testDataPath/sections.tex")
    }
}