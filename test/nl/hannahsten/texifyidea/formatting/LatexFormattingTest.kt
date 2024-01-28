package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.codestyle.LatexCodeStyleSettings
import nl.hannahsten.texifyidea.testutils.writeCommand

class LatexFormattingTest : BasePlatformTestCase() {

    /**
     * When having "\\\n", i.e. backslash newline, if that is a valid latex command then the next newline will not start a new line,
     * thus confuses the formatter.
     * So most probably this was a mistake. If ever someone comes up with a valid reason for using backslash newline, we can rethink this.
     */
    fun testBacklashNewline() {
        """
            Text hallo i.e.\
            \[
                \alpha
            \]
        """.trimIndent() `should be reformatted to` """
            Text hallo i.e.\
            \[
                \alpha
            \]
        """.trimIndent()
    }

    fun testVerbatim() {
        """
            \begin{verbatim}
            \end{verbatim}
        """.trimIndent() `should be reformatted to` """
            \begin{verbatim}
            \end{verbatim}
        """.trimIndent()
    }

    /**
     * This may not be what we want (though personally I've gotten used to it and think it's actually quite nice),
     * but having a test we at least notice when we (accidentally) change it.
     */
    fun `test labeled equation`() {
        """
            \begin{equation}\label{eq:xy}
                x = y
            \end{equation}
        """.trimIndent() `should be reformatted to` """
            \begin{equation}
                \label{eq:xy}
                x = y
            \end{equation}
        """.trimIndent()
    }

    fun `test environment parameters`() {
        """
            \begin{enumerate*}[label=(\roman*)]
                \item item1
                \item item2
            \end{enumerate*}
        """.trimIndent() `should be reformatted to` """
            \begin{enumerate*}[label=(\roman*)]
                \item item1
                \item item2
            \end{enumerate*}
        """.trimIndent()
    }

    fun `test leading comment in environment`() {
        """
            \begin{document}
                % This is a comment.
                This is real text.
            \end{document}
        """.trimIndent() `should be reformatted to` """
            \begin{document}
                % This is a comment.
                This is real text.
            \end{document}
        """.trimIndent()
    }

    fun `test angle parameter formatting`() {
        """
            \documentclass{beamer}
            
            \begin{document}
                \begin{frame}
                    \begin{block}{Title}<1->
                    Appel.
                    \end{block}
                    \begin{block}{Title}<2->
                    Peer.
                    \end{block}
                \end{frame}
            \end{document}
        """.trimIndent() `should be reformatted to` """
            \documentclass{beamer}
            
            \begin{document}
                \begin{frame}
                    \begin{block}{Title}<1->
                        Appel.
                    \end{block}
                    \begin{block}{Title}<2->
                        Peer.
                    \end{block}
                \end{frame}
            \end{document}
        """.trimIndent()
    }

    fun `test indentation in parameter`() {
        """
            \documentclass[
            12pt, a4
            ]{article}
            
            \newcommand{\bla}{%
            test
            }
            
            \tikzset{
            mystyle/.style={
            draw,
            circle,
            label={[fill=yellow]0:#1}
            }
            }
            
            {
            kaassoufflé
            }
        """.trimIndent() `should be reformatted to` """
            \documentclass[
                12pt, a4
            ]{article}
            
            \newcommand{\bla}{%
                test
            }
            
            \tikzset{
                mystyle/.style={
                    draw,
                    circle,
                    label={[fill=yellow]0:#1}
                }
            }
            
            {
                kaassoufflé
            }
        """.trimIndent()
    }

    fun `test formatter off and on comments`() {
        """
% @formatter:off
\begin{lstlisting}[language=Kotlin]
fun Int?.ifPositiveAddTwo(): Int =
        this?.let {
            if (this >= 0) this + 2
            else this
        } ?: 0
\end{lstlisting}
% @formatter:on
        """.trimIndent() `should be reformatted to` """
% @formatter:off
\begin{lstlisting}[language=Kotlin]
fun Int?.ifPositiveAddTwo(): Int =
        this?.let {
            if (this >= 0) this + 2
            else this
        } ?: 0
\end{lstlisting}
% @formatter:on
        """.trimIndent()
    }

    fun testAlgorithmicx() {
        """
            \begin{algorithm} \begin{algorithmic} \State begin 
            \If {$ i\geq maxval${'$'}} \State $ i\gets 0${'$'} 
            \Else \If {$ i+k\leq maxval${'$'}} \State $ i\gets i+k${'$'} 
            \EndIf 
            \EndIf \end{algorithmic} \caption{Insertion sort}\label{alg:algorithm2} \end{algorithm}
        """.trimIndent() `should be reformatted to` """
            \begin{algorithm}
                \begin{algorithmic}
                    \State begin
                    \If {$ i\geq maxval$}
                        \State $ i\gets 0$
                    \Else
                        \If {$ i+k\leq maxval$}
                            \State $ i\gets i+k$
                        \EndIf
                    \EndIf
                \end{algorithmic} \caption{Insertion sort}\label{alg:algorithm2}
            \end{algorithm}
        """.trimIndent()
    }

    fun testAlgorithm2e() {
        """
            \begin{algorithm} 
                 \While{While condition}{
        instructions\;
        \eIf{condition}{
            instructions1\;
            instructions2\;
        }{
            instructions3\;
        }
            }
             \end{algorithm}
        """.trimIndent() `should be reformatted to` """
            \begin{algorithm}
                \While{While condition}{
                    instructions\;
                    \eIf{condition}{
                        instructions1\;
                        instructions2\;
                    }{
                        instructions3\;
                    }
                }
            \end{algorithm}
        """.trimIndent()
    }

    fun `test section used in command definition`() {
        """
            \documentclass{article}
            \newcommand{\sectionlorem}[2]{\section{#1}\label{#2}}

            \begin{document}
                \sectionlorem{Title}{sec:label}
            \end{document}
        """.trimIndent() `should be reformatted to` """
            \documentclass{article}
            \newcommand{\sectionlorem}[2]{\section{#1}\label{#2}}

            \begin{document}
                \sectionlorem{Title}{sec:label}
            \end{document}
        """.trimIndent()
    }

    fun `test newlines before sectioning commands`() {
        """
            Text.
            \section{New section}
        """.trimIndent() `should be reformatted to` """
            Text.
            
            
            \section{New section}
        """.trimIndent()
    }

    private infix fun String.`should be reformatted to`(expected: String) {
        myFixture.configureByText(LatexFileType, this)
        writeCommand(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        myFixture.checkResult(expected)
    }
}