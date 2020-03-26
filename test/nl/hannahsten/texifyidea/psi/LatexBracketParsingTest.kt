package nl.hannahsten.texifyidea.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import org.junit.jupiter.api.Test

class LatexBracketParsingTest : BasePlatformTestCase() {

    @Test
    fun testBrackets() {
        myFixture.configureByText(LatexFileType, """
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
                text
                \begin{equation}
                    ]
                    \alpha\label[opt,opt2,opt={my3}][opt4]{eq:equation}[asdf]
                    text \left[  \right]

                    ~\ref{eq:equation}
                \end{equation}
            %[0,2)
                ${'$'}[0,2)${'$'}
            \end{document}
        """.trimIndent())

        myFixture.checkHighlighting()
    }
}