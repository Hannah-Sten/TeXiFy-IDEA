package nl.hannahsten.texifyidea.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.configureByFilesAndBuildFilesets
import nl.hannahsten.texifyidea.index.LatexProjectStructure
import nl.hannahsten.texifyidea.inspections.latex.probablebugs.LatexFileNotFoundInspection

class RootFileTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/util/rootfile"
    }

    fun testTwoLevelDeepInclusion() {
        myFixture.enableInspections(LatexFileNotFoundInspection())
        myFixture.configureByFilesAndBuildFilesets("contents/level-two/level-two.tex", "main.tex", "contents/level-one.tex", "contents/level-two/level-three.tex")
        // calls to rebuild the filesets
        myFixture.checkHighlighting()
    }
}