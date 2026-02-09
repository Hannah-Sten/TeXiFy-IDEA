package nl.hannahsten.texifyidea.index.file

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.documentation.LatexDocumentationProvider

class LatexDocsRegexerTest : BasePlatformTestCase() {

    fun testCommands() {
        val input = """
            The macro \cmd{\bblastx} will print the example number before last.
            This is like \cs{intertext} but uses shorter skips between the math. 
            The behaviour of the \pkg{siunitx} package is controlled by a number of key--value options. These can be given globally using the \cs{sisetup} function or locally as the optional argument to the user macros.
            \LaTeX{}
        """.trimIndent()
        val expected = """The macro <tt>\bblastx</tt> will print the example number before last.<br>This is like <tt>\intertext</tt> but uses shorter skips between the math. <br>The behaviour of the <tt>siunitx</tt> package is controlled by a number of key--value options. These can be given globally using the <tt>\sisetup</tt> function or locally as the optional argument to the user macros.<br>\LaTeX{}"""
        assertEquals(expected, LatexDocumentationProvider().formatDtxSource(input))
    }

    fun testShortVerbatim() {
        val input = """
            |\bibcase|. You can insert it anywhere and it will make the first letter of the following word either lower- or uppercase. For example, |opcit.bst| inserts |\bibcase| before the particle `in' of an |@INCOLLECTION| entry.
        """.trimIndent()
        val expected = """
            \bibcase. You can insert it anywhere and it will make the first letter of the following word either lower- or uppercase. For example, opcit.bst inserts \bibcase before the particle `in' of an @INCOLLECTION entry.
        """.trimIndent()
        assertEquals(expected, LatexDocumentationProvider().formatDtxSource(input))
    }

    fun testMargOarg() {
        // Should be left alone
        val input = """
            |\circlearc|\oarg{N}\marg{X}\marg{Y}\marg{RAD}\marg{ANGLE1}\marg{ANGLE2}\\
        """.trimIndent()
        val expected = """
            \circlearc\oarg{N}\marg{X}\marg{Y}\marg{RAD}\marg{ANGLE1}\marg{ANGLE2}\\
        """.trimIndent()
        assertEquals(expected, LatexDocumentationProvider().formatDtxSource(input))
    }

    fun testMarkup() {
        val input = """
            Do \textbf{not} eat a banana before bedtime.
            Bananas are \textit{evil}.
        """.trimIndent()
        val expected = """
            Do <b>not</b> eat a banana before bedtime.<br>Bananas are <i>evil</i>.
        """.trimIndent()
        assertEquals(expected, LatexDocumentationProvider().formatDtxSource(input))
    }
}