package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkObject
import nl.hannahsten.texifyidea.index.LatexProjectStructure
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

        myFixture.configureByFiles("contents/level-two/level-two.tex", "main.tex", "contents/level-one.tex", "contents/level-two/level-three.tex")
//        myFixture.configureByFiles("main.tex", "contents/level-one.tex", "contents/level-two/level-three.tex")
        // calls to rebuild the filesets
        LatexProjectStructure.buildFilesetsNow(myFixture.project)
        myFixture.checkHighlighting()
    }
}