package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType

class LatexParserTest : BasePlatformTestCase() {

    fun testNestedInlineMath() {
        myFixture.configureByText(LatexFileType, """
            $ math \text{ text $\xi$ text } math$
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testIfnextchar() {
        myFixture.configureByText(LatexFileType, """
            \newcommand{\xyz}{\@ifnextchar[{\@xyz}{\@xyz[default]}}
            \def\@xyz[#1]#2{do something with #1 and #2}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testArrayPreambleOptions() {
        myFixture.configureByText(LatexFileType, """
            \begin{tabular}{l >{$}l<{$}}
                some text & y = x (or any math) \\
                more text & z = 2 (or any math) \\
            \end{tabular}
        """.trimIndent())
        myFixture.checkHighlighting()
    }

    fun testInlineVerbatim() {
        myFixture.configureByText(LatexFileType, """
            \verb| aaa $ { bbb | text $\xi$ not verb: |a|
            \verb|aaa $ {" bbb| text $\xi$ 
            \verb!$|! 
            \verb=}{= 
            \verb"}$"
            \verb|%md|
            \verb*|$|
            \lstinline|$|
        """.trimIndent())
        myFixture.checkHighlighting()
    }
}