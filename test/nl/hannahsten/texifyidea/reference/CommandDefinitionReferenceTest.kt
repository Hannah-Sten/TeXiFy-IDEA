package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class CommandDefinitionReferenceTest : BasePlatformTestCase() {

    fun testRename() {
        myFixture.configureByText(LatexFileType, """\newcommand{\joepsie}{} \joepsie<caret>""")
        myFixture.renameElementAtCaret("\\mooizo")
        myFixture.checkResult("""\newcommand{\mooizo}{} \mooizo""")
    }
}