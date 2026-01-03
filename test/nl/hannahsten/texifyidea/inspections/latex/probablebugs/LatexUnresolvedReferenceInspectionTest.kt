package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import nl.hannahsten.texifyidea.configureByFilesAndBuildFilesets
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.updateCommandDef
import nl.hannahsten.texifyidea.updateFilesets
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexUnresolvedReferenceInspectionTest : TexifyInspectionTestBase(LatexUnresolvedReferenceInspection()) {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    override fun getTestDataPath(): String = "test/resources/inspections/latex/unresolvedreference"

    fun testWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \ref{<warning descr="Unresolved reference 'alsonot'">alsonot</warning>}
            \cite{<warning descr="Unresolved reference 'nope'">nope</warning>}
            
            \newcommand*{\citewithauthor}[1]{\citeauthor{#1}~\cite{#1}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNoWarning() {
        myFixture.configureByText(
            LatexFileType,
            """
            \label{alabel}
            \ref{alabel}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testBibtexReference() {
        try {
            val name = getTestName(false) + ".tex"
            myFixture.configureByFilesAndBuildFilesets(name, "references.bib")

            myFixture.checkHighlighting()
        }
        finally {
            clearAllMocks()
            unmockkAll()
        }
    }

    // Was implemented previously but not yet implemented back after refactoring (TEX-243)
//    fun testNoWarningCustomCommandWithPrefix() {
//        myFixture.configureByText(
//            LatexFileType,
//            """
//            \newcommand{\mylabel}[1]{\label{sec:#1}}
//            \section{some sec}\mylabel{some-sec}
//            ~\ref{sec:some-sec}
//            """.trimIndent()
//        )
//        myFixture.updateCommandDef()
//        myFixture.checkHighlighting()
//    }
//
//    fun testFigureReferencedCustomCommandOptionalParameter() {
//        myFixture.configureByText(
//            LatexFileType,
//            """
//            \newcommand{\includenamedimage}[3][]{
//            \begin{figure}
//                \centering
//                \includegraphics[width=#1\textwidth]{#2}
//                \caption{#3}
//                \label{fig:#2}
//            \end{figure}
//            }
//
//            \includenamedimage[0.5]{test.png}{fancy caption}
//            \includenamedimage{test2.png}{fancy caption}
//
//            some text~\ref{fig:test.png} more text.
//            some text~\ref{fig:test2.png} more text.
//            """.trimIndent()
//        )
//        myFixture.updateCommandDef()
//        myFixture.checkHighlighting()
//    }e

    fun testFigureReferencedCustomListingsEnvironment() {
        myFixture.configureByText(
            LatexFileType,
            """
            \lstnewenvironment{java}[2][]{
                \lstset{
                    captionpos=b,
                    language=Java,
                % other style attributes
                    caption={#1},
                    label={#2},
                }
            }{}
            
            \begin{<caret>}[Test]{lst:test}
                class Main {
                    public static void main(String[] args) {
                        return "HelloWorld";
                    }
                }
            \end{java}

            \ref{lst:test}
            """.trimIndent()
        )
        // The environment label is saved with the stub but it requires the semantics to be already present in cache, so we first update semantics and then type something to trigger stub creation with label
        myFixture.updateCommandDef()
        myFixture.type("java")
        myFixture.checkHighlighting()
    }

    fun testNoWarningCustomCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\mylabell}[1]{\label{#1}}
            \section{some sec}\mylabell{some-sec}
            ~\ref{some-sec}
            """.trimIndent()
        )
        myFixture.updateCommandDef()
        myFixture.checkHighlighting()
    }

    fun `test using xr package`() {
        myFixture.copyFileToProject("presentations/presentation.tex")
        myFixture.configureByFiles("xr-test.tex")
        myFixture.updateFilesets()
        myFixture.checkHighlighting()
    }

    fun testComma() {
        myFixture.configureByText(LatexFileType, """\input{name,with,.tex}""")
        myFixture.checkHighlighting()
    }

    fun testNewcommand() {
        myFixture.configureByText(LatexFileType, """\newcommand{\bla}[1]{\includegraphics{#1}}""")
        myFixture.checkHighlighting()
    }
}