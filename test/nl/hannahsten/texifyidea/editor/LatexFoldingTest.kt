package nl.hannahsten.texifyidea.editor

import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Note that folding builders need to implement DumbAware.
 */
class LatexFoldingTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/editor/folding"

    fun testDashFolding() {
        // Unicode issues on windows
        if (!SystemInfo.isWindows) {
            myFixture.testFoldingWithCollapseStatus("$testDataPath/dash.tex")
        }
    }

    fun testEnvironmentFolding() {
        myFixture.testFolding("$testDataPath/environment.tex")
    }

    fun testEscapedSymbolFolding() {
        myFixture.testFolding("$testDataPath/escaped-symbols.tex")
    }

    fun testMathSymbolFolding() {
        // Unicode issues on windows
        if (!SystemInfo.isWindows) {
            myFixture.testFolding("$testDataPath/math-symbols.tex")
        }
    }

    fun testMathSymbolFoldingInEnvironment() {
        // Unicode issues on windows
        if (!SystemInfo.isWindows) {
            myFixture.testFolding("$testDataPath/math-symbols-environment.tex")
        }
    }

    fun testSectionFolding() {
        myFixture.testFolding("$testDataPath/sections.tex")
    }

    fun testSectionEndingFolding() {
        myFixture.testFolding("$testDataPath/sections-ending.tex")
    }

    fun testCustomFoldingRegions() {
        myFixture.testFolding("$testDataPath/custom-regions.tex")
    }

    fun testCommentFolding() {
        myFixture.testFolding("$testDataPath/comments.tex")
    }

    fun testLeftRightFolding() {
        myFixture.testFolding("$testDataPath/left-right.tex")
    }

    fun testMathStyleFolding() {
        // Unicode issues on windows
        if (!SystemInfo.isWindows) {
            myFixture.testFolding("$testDataPath/math-style.tex")
        }
    }
}