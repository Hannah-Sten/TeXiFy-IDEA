package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.every
import io.mockk.mockkStatic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.util.parser.collectSubtreeTyped
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
            
            $\test{\cmd{a}[b]}$
            
            \newcolumntype{P}[1]{>{\raggedright\arraybackslash}p{#1}}
            
            \anycommand{test = {Some text with (Round Brackets)}}
            
            \href{\thefield{#%}}{#1}
            
            \begin{maxi!}|l|[3] \end{maxi!}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testMismatchedMathBrackets() {
        myFixture.configureByText(
            LatexFileType,
            """
            $[0,1)$
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
            \def\bracefill{$\hbox{$#4$}$} % MnSymbol.sty
            
            
            $\begin{cases*}
                 1 & if $ p \equiv 1 \pmod 4$ \\
                 -1 & if $ p \equiv 3 \pmod 4$
            \end{cases*}$ a
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testIfnextchar() {
        myFixture.configureByText(
            LatexFileType,
            $$"""
            \newcommand{\xyz}{\@ifnextchar[{\@xyz}{\@xyz[default]}}
            \def\@xyz[#1]#2{do something with #1 and #2}
            
            \@namedef{#1}{\@ifnextchar{^}{\@nameuse{#1@}}{\@nameuse{#1@}^{}}}
            
            \newcommand{\abc}{\@ifnextchar${Math coming: }{No math}}
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
                \only<1>{<info descr="null">$<info textAttributesKey=LATEX_INLINE_MATH>a_1$</info></info>}
            \end{frame}
            
            \tikzset{<->/.style=->}
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
            \ifdog DOG \else CAT \fi
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
            $$"""
            \newcommand{\tableMod}[0]{
                \end{multicols}
                \insertedObject
                \begin{multicols}{2}
            }
            \newcommand{\cmd}{$x$}
            \newcommand\MnMissing{$\times$} % MnSymbol package
            
            \AfterEndEnvironment{minted}{
                \end{tcolorbox}
            }
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testMultipleUnclosedParametersDoNotCauseExponentialBacktracking() {
        // Generate a string like "\cmd{ \cmd{ \cmd{ \cmd{ \cmd{ \cmd{ \cmd{ \cmd{"
        // The hasMatchingCloseBrace predicate ensures unclosed braces are not parsed as required_param.
        // This test verifies the PSI tree structure is correct for this edge case.
        val depth = 16
        val unclosedCommands = (1..depth).joinToString(" ") { "\\cmd{" }

        val psiFile = myFixture.configureByText(
            LatexFileType,
            unclosedCommands
        )

        // Verify the PSI tree structure is correct:
        // 1. We should have exactly `depth` commands (one per \cmd)
        // 2. None of the { should be parsed as required_param (since they're unclosed
        //    and the hasMatchingCloseBrace predicate prevents parsing them as such)

        val commands = psiFile.collectSubtreeTyped<LatexCommands>()
        assertEquals(
            "Expected $depth commands, but found ${commands.size}. " +
                "This might indicate incorrect parsing.",
            depth,
            commands.size
        )

        val requiredParams = psiFile.collectSubtreeTyped<LatexRequiredParam>()

        assertEquals(
            "Expected 0 required params (all braces are unclosed), but found ${requiredParams.size}",
            0,
            requiredParams.size
        )
    }

    fun testBracesInCommentsDoNotAffectLookahead() {
        // The } in the comment should not be counted as matching the outer {
        myFixture.configureByText(
            LatexFileType,
            """
            \cmd{text % } this brace is in a comment
            more text}
            \another{param}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testBracesInVerbatimDoNotAffectLookahead() {
        myFixture.configureByText(
            LatexFileType,
            """
            \cmd{before}
            \begin{verbatim}
            } { } { unmatched braces in verbatim
            \end{verbatim}
            \another{after}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testEscapedBracesDoNotAffectLookahead() {
        myFixture.configureByText(
            LatexFileType,
            """
            \cmd{text with \} escaped close brace}
            \another{text with \{ escaped open brace}
            \third{\{ and \} both escaped}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testInlineVerbatimBracesDoNotAffectLookahead() {
        myFixture.configureByText(
            LatexFileType,
            """
            \cmd{before \verb|}{| after}
            \another{text}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNestedCommandsWithMatchedBraces() {
        myFixture.configureByText(
            LatexFileType,
            """
            \outer{\inner{\deepest{text}}}
            \cmd{\textbf{\textit{nested formatting}}}
            \section{Title with \emph{emphasis}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testMixedMatchedAndUnmatchedBraces() {
        val psiFile = myFixture.configureByText(
            LatexFileType,
            """
            \cmd{properly matched}
            \another{ this one is not closed
            \third{also matched}
            """.trimIndent()
        )

        // Verify that matched braces are parsed as required_param, but unclosed ones are not
        val commands = psiFile.collectSubtreeTyped<LatexCommands>()
        assertEquals(3, commands.size)

        val requiredParams = psiFile.collectSubtreeTyped<LatexRequiredParam>()
        // Only \cmd and \third have matched braces, \another does not
        assertEquals(2, requiredParams.size)
    }

    fun testVerbWithBracesInContent() {
        myFixture.configureByText(
            LatexFileType,
            """
            Text before \verb|{ } { }| text after
            \verb+{}+ also works
            \cmd{normal param}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testLstinlineWithPipeDelimiter() {
        myFixture.configureByText(
            LatexFileType,
            """
            \lstinline|code with { and } inside|
            \cmd{normal param}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testLstinlineWithBraceDelimiterSimple() {
        myFixture.configureByText(
            LatexFileType,
            """
            \lstinline{simple code without braces}
            \cmd{normal param}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testUrlWithNestedBraces() {
        myFixture.configureByText(
            LatexFileType,
            """
            \url{http://example.com/path?param={value}}
            \cmd{normal param}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testCombinedBraceEdgeCases() {
        myFixture.configureByText(
            LatexFileType,
            """
            \section{Title with \textbf{bold} and \emph{emphasis}}

            Some text with \{ escaped braces \} in it.

            \begin{lstlisting}[language=Java]
            public void method() { // comment with }
                System.out.println("{nested}");
            }
            \end{lstlisting}

            Back to normal \cmd{with % } comment in param
            continuation of param}

            \verb|special { chars }| and more text.

            \href{http://example.com}{Link with \textbf{bold}}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testNewenvironmentWithUnmatchedBeginEnd() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newenvironment{myenv}
                {\begin{center}\bfseries}
                {\end{center}}

            \begin{myenv}
                Content here
            \end{myenv}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}