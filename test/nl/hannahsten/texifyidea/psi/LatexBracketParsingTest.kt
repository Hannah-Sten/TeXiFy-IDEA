package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.intellij.lang.annotations.Language

class LatexBracketParsingTest : BasePlatformTestCase() {

    fun testBrackets() {
        @Language("Latex")
        val text =
            """
            \documentclass{article}

            \begin{document}
                \include[option]{included}

                \begin{equation}[opts]
                    adsf
                \end{equation}

                ]
                [

                \[
                    ] [   asd]

                    \label[opt]{eq:equation}[]

                    ~\ref{eq:equation}
                \]
                
                \[
                    [
                \]
                
                \begin{center}
                    [
                \end{center}

                text
                \begin{equation}
                    ]
                    \alpha\label[opt,opt2,opt={my3}][opt4]{eq:equation}[asdf]
                    text \left[  \right]

                    ~\ref{eq:equation}
                \end{equation}
            %[0,2)
                $[0,2)$
            \end{document}
            """.trimIndent()

        myFixture.configureByText(LatexFileType, text)
        myFixture.checkHighlighting()
    }
}