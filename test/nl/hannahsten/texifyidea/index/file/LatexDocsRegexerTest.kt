package nl.hannahsten.texifyidea.index.file

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class LatexDocsRegexerTest : BasePlatformTestCase() {

    fun testCommands() {
        val input = """
            The macro \cmd{\bblastx} will print the example number before last.
            This is like \cs{intertext} but uses shorter skips between the math. 
            The behaviour of the \pkg{siunitx} package is controlled by a number of key--value options. These can be given globally using the \cs{sisetup} function or locally as the optional argument to the user macros.
            \LaTeX{}
        """.trimIndent()
        val expected = """The macro \bblastx will print the example number before last.<br>This is like \intertext but uses shorter skips between the math. <br>The behaviour of the siunitx package is controlled by a number of key--value options. These can be given globally using the \sisetup function or locally as the optional argument to the user macros.<br>\LaTeX{}"""
        assertEquals(expected, LatexDocsRegexer.format(input))
    }

    fun testShortVerbatim() {
        val input = """
            |\bibcase|. You can insert it anywhere and it will make the first letter of the following word either lower- or uppercase. For example, |opcit.bst| inserts |\bibcase| before the particle `in' of an |@INCOLLECTION| entry.
        """.trimIndent()
        val expected = """
            \bibcase. You can insert it anywhere and it will make the first letter of the following word either lower- or uppercase. For example, opcit.bst inserts \bibcase before the particle `in' of an @INCOLLECTION entry.
        """.trimIndent()
        assertEquals(expected, LatexDocsRegexer.format(input))
    }

    fun testMargOarg() {
        // Should be left alone, see LatexCommand
        val input = """
            |\circlearc|\oarg{N}\marg{X}\marg{Y}\marg{RAD}\marg{ANGLE1}\marg{ANGLE2}\\
        """.trimIndent()
        val expected = """
            \circlearc\oarg{N}\marg{X}\marg{Y}\marg{RAD}\marg{ANGLE1}\marg{ANGLE2}\\
        """.trimIndent()
        assertEquals(expected, LatexDocsRegexer.format(input))
    }
}