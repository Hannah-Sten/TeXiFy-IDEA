package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class CommandDefinitionReferenceTest : BasePlatformTestCase() {

    fun testRename() {
        myFixture.configureByText(LatexFileType, """\newcommand{\joepsie}{} \joepsie<caret>""")
        myFixture.renameElementAtCaret("\\mooizo")
        myFixture.checkResult("""\newcommand{\mooizo}{} \mooizo""")
    }

    fun testResolveInDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\MyCommand}{Hello World!}
            \newcommand{\AnotherCommand}{\MyCommand<caret>}
            \AnotherCommand
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("\\floep")
        myFixture.checkResult(
            """
            \newcommand{\floep}{Hello World!}
            \newcommand{\AnotherCommand}{\floep<caret>}
            \AnotherCommand
            """.trimIndent()
        )
    }
}