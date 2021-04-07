package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.inspections.latex.probablebugs.LatexFileNotFoundInspection

class RootFileTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/util/rootfile"
    }

    fun testTwoLevelDeepInclusion() {
        myFixture.enableInspections(LatexFileNotFoundInspection())
        myFixture.copyFileToProject("main.tex")
        myFixture.copyFileToProject("contents/level-one.tex")
        myFixture.copyFileToProject("contents/level-two/level-three.tex")
        myFixture.configureByFile("contents/level-two/level-two.tex")
        myFixture.checkHighlighting()
    }
}