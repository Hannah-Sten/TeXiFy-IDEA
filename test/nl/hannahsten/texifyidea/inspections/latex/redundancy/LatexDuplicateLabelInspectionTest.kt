package nl.hannahsten.texifyidea.inspections.latex.redundancy

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexDuplicateLabelInspectionTest : TexifyInspectionTestBase(LatexDuplicateLabelInspection()) {

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \label{<error descr="Duplicate label 'some-label'">some-label</error>}
            \label{<error descr="Duplicate label 'some-label'">some-label</error>}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testFigureReferencedCustomCommandOptionalParameter() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\includenamedimage}[3][]{
            \begin{figure}
                \centering
                \includegraphics[width=#1\textwidth]{#2}
                \caption{#3}
                \label{fig:#2}
            \end{figure}
            }
        
            \includenamedimage[0.5]{test.png}{fancy caption}
            \includenamedimage{test2.png}{fancy caption}
        
            some text~\ref{fig:test.png} more text.
            some text~\ref{fig:test2.png} more text.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testDuplicateLabelWithEnvironmentAndCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \label{<error descr="Duplicate label 'some-label'">some-label</error>}
            \begin{lstlisting}[label=<error descr="Duplicate label 'some-label'">{some-label}</error>]
            \end{lstlisting}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testDuplicateLabelWithCommandAndCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \label{<error descr="Duplicate label 'some-label'">some-label</error>}
            \lstinputlisting[label=<error descr="Duplicate label 'some-label'">some-label</error>]{some/file}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testDuplicateLabelBetweenEnvironments() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{lstlisting}[label=<error descr="Duplicate label 'some-label'">some-label</error>]
            \end{lstlisting}
            \begin{lstlisting}[label=<error descr="Duplicate label 'some-label'">some-label</error>]
            \end{lstlisting}                        
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoErrorForDuplicateLabelInCommandDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\dummy}{\label{#1}}
            \newcommand{\dummy}{\label{#1}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoErrorForDuplicateLabelInEnvironmentDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{\dummy}[1]{\label{#1}}{}
            \newenvironment{\dummy}[1]{\label{#1}}{}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testIfs() {
        myFixture.configureByText(
            LatexFileType,
            """
                \ifdog
                \section{DOG}\label{sec:pet}
                \else
                \section{CAT}\label{sec:pet}
                \fi
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}