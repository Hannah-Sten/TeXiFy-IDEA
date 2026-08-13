package nl.hannahsten.texifyidea.gutter

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef

class LatexElementColorProviderTest : BasePlatformTestCase() {

    fun testInfiniteColorLoop() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{xcolor}
            \colorlet{kameel}{oliefant}
            \colorlet{oliefant}{kameel}
            """.trimIndent()
        )
        myFixture.findAllGutters()
    }

    fun testStandardColorName() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{color}
            \definecolor{green}{HTML}{FFFFFF}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        assertEquals(2, myFixture.findAllGutters().size)
    }

    fun testCustomColorName() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{color}
            \definecolor{custom}{HTML}{FFFFFF}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        assertEquals(2, myFixture.findAllGutters().size)
    }

    fun testCustomColorUsage() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{color}
            \definecolor{custom}{HTML}{FFFFFF}
            \color{custom}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        assertEquals(3, myFixture.findAllGutters().size)
    }
}