package nl.hannahsten.texifyidea.reference

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.requireIs
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
        myFixture.checkResult(
            """
            \ref{file1_label}<caret>
            """.trimIndent()
        )

        myFixture.configureByText("file2.tex", "\\ref{file1_label<caret>}")
        myFixture.renameElementAtCaret("label_renamed")

        myFixture.checkResult("file1_nested.tex", "\\label{label_renamed}", true)
    }

    fun testLabelWithXRPackage() {
        val main = """
            \documentclass[11pt]{article}
            \usepackage{xr,xr-hyper,hyperref}
            % Note: name of the aux file in the output directory, so no full path
            \externaldocument[P-]{presentation}

            \begin{document}
                Slide~\ref{P-slide:<caret>first} is referenced from the main document.
                
                \label{slide:first} not this one.
            \end{document}
        """.trimIndent()

        val presentation = """
            \documentclass{beamer}
            \usepackage{hyperref}

            \begin{document}
                \begin{frame}
                    This is not slide~\ref{slide:first}.
                \end{frame}
                \begin{frame}
                    \label{slide:first}
                    This is slide~\ref{slide:first}.
                \end{frame}
            \end{document}
        """.trimIndent()

        myFixture.createFile("presentation.tex", presentation)
        val mainFile = myFixture.createFile("main.tex", main)
        myFixture.updateFilesets()
        val ref = myFixture.getReferenceAtCaretPosition("main.tex").requireIs<LatexLabelParameterReference>()
        val labelText = ref.resolve()!!
        assertEquals(labelText.containingFile.name, "presentation.tex")
        assertEquals(labelText.text, "slide:first")

        myFixture.openFileInEditor(mainFile)
        myFixture.renameElementAtCaret("slide:renamed")
        myFixture.checkResult(
            "main.tex",
            """
            \documentclass[11pt]{article}
            \usepackage{xr,xr-hyper,hyperref}
            % Note: name of the aux file in the output directory, so no full path
            \externaldocument[P-]{presentation}

            \begin{document}
                Slide~\ref{P-slide:renamed} is referenced from the main document.
                
                \label{slide:first} not this one.
            \end{document}
            """.trimIndent(),
            true
        )
        myFixture.checkResult(
            "presentation.tex",
            """
            \documentclass{beamer}
            \usepackage{hyperref}

            \begin{document}
                \begin{frame}
                    This is not slide~\ref{slide:renamed}.
                \end{frame}
                \begin{frame}
                    \label{slide:renamed}
                    This is slide~\ref{slide:renamed}.
                \end{frame}
            \end{document}
            """.trimIndent(),
            true
        )
    }
}