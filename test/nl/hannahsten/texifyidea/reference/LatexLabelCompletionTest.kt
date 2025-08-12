package nl.hannahsten.texifyidea.reference

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.updateCommandDef
import org.junit.Test

class LatexLabelCompletionTest : BasePlatformTestCase() {

    @Test
    fun testLabelReferenceCompletion() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \begin{figure}
                    \label{fig:figure}
                \end{figure}
                \begin{lstlisting}[label={lst:listing}]
                    Some text
                \end{lstlisting}
                \section{some section}
                \label{sec:some-section}
                \ref{<caret>}
            \end{document}
            """.trimIndent()
        )

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(3, result.size)
        assertTrue(result.any { l -> l.lookupString == "fig:figure" })
        assertTrue(result.any { l -> l.lookupString == "lst:listing" })
        assertTrue(result.any { l -> l.lookupString == "sec:some-section" })
    }

    @Test
    fun testCommandParameterLabelReferenceCompletion() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \lstinputlisting[label={lst:inputlisting}]{some/file}
                \ref{<caret>}
            \end{document}
            """.trimIndent()
        )

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(1, result.size)
        assertTrue(result.any { l -> l.lookupString == "lst:inputlisting" })
    }

    fun testSecondLabelParameter() {
        // given
        myFixture.configureByText(
            LatexFileType,
            """
            \usepackage{cleveref}
            \begin{document}
                \label{blub}
                \label{kameel}
                \crefrange{blub}{<caret>}
            \end{document}
            """.trimIndent()
        )
        myFixture.updateCommandDef()

        // when
        val result = myFixture.complete(CompletionType.BASIC)

        // then
        assertEquals(2, result.size)
        assertTrue(result.any { l -> l.lookupString == "kameel" })
    }
}