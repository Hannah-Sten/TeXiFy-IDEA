package nl.hannahsten.texifyidea.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class BibtexFoldingTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/editor/folding"

    fun testDashFolding() {
        myFixture.testFoldingWithCollapseStatus("$testDataPath/references.bib")
    }
}