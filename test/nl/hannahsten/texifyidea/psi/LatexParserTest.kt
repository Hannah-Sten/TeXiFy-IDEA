package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.runCommandWithExitCode

class LatexParserTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        mockkStatic(::runCommandWithExitCode)
        every { runCommandWithExitCode(*anyVararg(), workingDirectory = any(), timeout = any(), returnExceptionMessage = any()) } returns Pair(null, 0)
    }

    fun testSomeGeneralConstructs() {
        myFixture.configureByText(
            LatexFileType,
            """
            \mycommand{[test]}
            \c{[}
            
            ${'$'}\test{\cmd{a}[b]}${'$'}
            
            \newcolumntype{P}[1]{>{\raggedright\arraybackslash}p{#1}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testMismatchedMathBrackets() {
        myFixture.configureByText(
            LatexFileType,
            """
            ${'$'}[0,1)${'$'}
            \[ ] \]
            \begin{equation}
                ]
            \end{equation}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNestedInlineMath() {
        myFixture.configureByText(
            LatexFileType,
            """
            $ math \text{ text $\xi$ text } math$
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testIfnextchar() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\xyz}{\@ifnextchar[{\@xyz}{\@xyz[default]}}
            \def\@xyz[#1]#2{do something with #1 and #2}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testArrayPreambleOptions() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{keyword}{\leavevmode\color{magenta}}{}
            \begin{tabular}{l >{$}l<{$} >{\begin{keyword}} l <{\end{keyword}} }
                some text & y = x (or any math) & prop1 \\
                more text & z = 2 (or any math) & prop2 \\
            \end{tabular}
            Fake preamble option:
            \begin{tikzpicture}
            \visible<+->{\node (a) at (0, 0) {$ \{A\}_{j}$ };}
            \node (b) at (1, 1) { $ B $ };
            \end{tikzpicture}
            Similar but with different commands:
            \pretitle{\begin{center}\fontsize{18bp}{18bp}\selectfont}
            \posttitle{\par\end{center}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testFakeArrayPreambleOptions() {
        myFixture.configureByText(
            LatexFileType,
            """
            <info descr="null">% Not a preamble option, so treat like usual</info>
            \begin{frame}
                \only<1>{<info descr="null">${'$'}<info textAttributesKey=LATEX_INLINE_MATH>a_1${'$'}</info></info>}
            \end{frame}
            """.trimIndent()
        )
        myFixture.checkHighlighting(false, true, false)
    }

    fun testNewEnvironmentDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{test}{\begin{center}}{\end{center}}
            \newenvironment{test2}{ \[ }{ \] }
            \newenvironment{test2}{ $ x$ and $ }{ $ }
            \newenvironment{test}[1]{\begin{test*}{#1}}{\end{test*}}
            $\xi$
            
            \begin{document}
                \newenvironment{test}{\begin{center}}{\end{center}}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNewDocumentEnvironmentDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{xparse}
            \NewDocumentEnvironment{name}{args spec}{\begin{center}{arg}}{\end{center}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testInlineVerbatim() {
        myFixture.configureByText(
            LatexFileType,
            """
            \verb| aaa $ { bbb | text $\xi$ not verb: |a|
            \verb|aaa $ {" bbb| text $\xi$ 
            \verb!$|! 
            \verb=}{= 
            \verb"}$"
            \verb|%md|
            \verb*|$|
            \verb$|$
            \verb-afdsa$-
            \lstinline|$|
            \lstinline{$}
            \lstinline[language=Fortran]{$}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testVerbatim() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{verbatim}
                $
                \end{no verbatim} }
                \begin{alsonoverbatim}
                \end}
                \end{}
                \end\asdf
            \end{verbatim}
            
            $ math$
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testLexerOffOn() {
        myFixture.configureByText(
            LatexFileType,
            """
            %! parser = off bla
                \end{verbatim} \verb| 
            % !TeX parser = on comment
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testBeginEndPseudoCodeBlock() {
        // A single begin or end pseudocode block should not be a parse error
        // because it might not be a pseudocode command at all (but the lexer doesn't know)
        myFixture.configureByText(
            LatexFileType,
            """
            I write \State \Until I \Repeat \EndProcedure.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNestedVerbatimBrackets() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{lstlisting}[language={[x86masm]Assembler},label={lst:lstlisting}]
                push   %rbp
            \end{lstlisting}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testAlgorithm2e() {
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{algorithm2e}
            \begin{document}
                \begin{algorithm*}
                    \uIf{condition}{
                        \If{condition}{
                            continue
                        }
                    }
                    \Else{
                        continue
                    }
                \end{algorithm*}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testAlgorithmIfElseIf() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{algorithm}
                \begin{algorithmic}
                    \Procedure{Euclid}{a,b}
                    \If{a<0 or b < 0}
                    \ElsIf{a,b<0}
                    \Else
                    \EndIf
                    \EndProcedure
                \end{algorithmic}
            \end{algorithm}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testUnmatchedBeginInDefinition() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\tableMod}[0]{
                \end{multicols}
                \insertedObject
                \begin{multicols}{2}
            }
            \newcommand{\cmd}{${'$'}x${'$'}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}