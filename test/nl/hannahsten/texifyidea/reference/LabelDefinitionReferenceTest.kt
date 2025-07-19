package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateFilesets

class LabelDefinitionReferenceTest : BasePlatformTestCase() {

    fun `test rename of label command`() {
        myFixture.configureByText(LatexFileType, """\label{test} \ref{test<caret>}""")
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult("""\label{renamed} \ref{renamed<caret>}""")
    }

    fun `test rename of label in environment`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{lstlisting}[label=test]
            \end{lstlisting}
            \ref{test<caret>}
            """.trimMargin()
        )
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \begin{lstlisting}[label=renamed]
            \end{lstlisting}
            \ref{renamed<caret>}
            """.trimMargin()
        )
    }

    fun `test rename of label in environment with special character`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{lstlisting}[label=test,escapechar=|!\"&]
            \end{lstlisting}
            \ref{test<caret>}
            """.trimMargin()
        )
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \begin{lstlisting}[label=renamed,escapechar=|!\"&]
            \end{lstlisting}
            \ref{renamed<caret>}
            """.trimMargin()
        )
    }

    fun `test rename of label in command`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \lstinputlisting[label=test]{inputfile}
            \ref{test<caret>}
            """
                .trimMargin()
        )
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \lstinputlisting[label=renamed]{inputfile}
            \ref{renamed<caret>}
            """.trimMargin()
        )
    }

    fun `test rename of label in group`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \lstinputlisting[label={test}]{inputfile}
            \ref{test<caret>}
            """
                .trimMargin()
        )
        myFixture.renameElementAtCaret("renamed")
        myFixture.checkResult(
            """
            \lstinputlisting[label={renamed}]{inputfile}
            \ref{renamed<caret>}
            """.trimMargin()
        )
    }

    fun testReferenceToLabelInFileset() {
        val main = """
            \documentclass{article}
            \begin{document}
            \input{file1}
            \input{file2}
            \end{document}
        """.trimIndent()

        val file1 = """
            \input{file1_nested}
            \ref{file1_label}
        """.trimIndent()

        val file1Nested = """
            \label{file1_label}
        """.trimIndent()


        myFixture.createFile("main.tex", main)
        myFixture.createFile("file1.tex", file1)
        myFixture.createFile("file1_nested.tex", file1Nested)
        myFixture.configureByText("file2.tex", "\\ref{file1<caret>}")
        myFixture.updateFilesets()
        myFixture.completeBasic()
        myFixture.checkResult("""
            \ref{file1_label}<caret>
        """.trimIndent())

        myFixture.configureByText("file2.tex", "\\ref{file1_label<caret>}")
        myFixture.renameElementAtCaret("label_renamed")

        myFixture.checkResult("file1_nested.tex", "\\label{label_renamed}", true)
    }
}