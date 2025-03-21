package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import nl.hannahsten.texifyidea.configureByFilesWithMockCache
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.lang.alias.EnvironmentManager
import nl.hannahsten.texifyidea.util.runCommandWithExitCode
import org.junit.Test

class LatexUnresolvedReferenceInspectionTest : TexifyInspectionTestBase(LatexUnresolvedReferenceInspection()) {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/unresolvedreference"
    }

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

    fun testNoWarningCustomCommand() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\mylabell}[1]{\label{#1}}
            \section{some sec}\mylabell{some-sec}
            ~\ref{some-sec}
            """.trimIndent()
        )
        CommandManager.updateAliases(setOf("\\label"), project)
        myFixture.checkHighlighting()
    }

    fun testNoWarningCustomCommandWithPrefix() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\mylabel}[1]{\label{sec:#1}}
            \section{some sec}\mylabel{some-sec}
            ~\ref{sec:some-sec}
            """.trimIndent()
        )
        CommandManager.updateAliases(setOf("\\mylabel"), project)
        myFixture.checkHighlighting()
    }

    fun testBibtexReference() {
        try {
            val name = getTestName(false) + ".tex"
            myFixture.configureByFilesWithMockCache(name, "references.bib")

            myFixture.checkHighlighting()
        }
        finally {
            clearAllMocks()
            unmockkAll()
        }
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
        CommandManager.updateAliases(setOf("\\label"), project)
        myFixture.checkHighlighting()
    }

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

            \begin{java}[Test]{lst:test}
                class Main {
                    public static void main(String[] args) {
                        return "HelloWorld";
                    }
                }
            \end{java}

            \ref{lst:test}
            """.trimIndent()
        )
        EnvironmentManager.updateAliases(setOf("lstlisting"), project)
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

    @Test
    fun `test using xr package`() {
        myFixture.copyFileToProject("presentations/presentation.tex")
        myFixture.configureByFiles("xr-test.tex")
        myFixture.checkHighlighting()
    }
}