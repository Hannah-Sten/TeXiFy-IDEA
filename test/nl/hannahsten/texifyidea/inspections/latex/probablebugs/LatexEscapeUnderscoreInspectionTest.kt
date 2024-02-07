package nl.hannahsten.texifyidea.inspections.latex.probablebugs

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

internal class LatexEscapeUnderscoreInspectionTest : TexifyInspectionTestBase(LatexEscapeUnderscoreInspection()) {

    fun `test unescaped _ character triggers warning in normal text`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                some text <warning descr="Escape character \ expected">_</warning> with unescaped underscore character
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers warning in section title`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \section{some title <warning descr="Escape character \ expected">_</warning> with unescaped underscore character}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers warning in captions`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \begin{figure}
                  \caption{A picture of a <warning descr="Escape character \ expected">_</warning>}
                \end{figure}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers warning in textit`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                italic \textit{<warning descr="Escape character \ expected">_</warning>} underscore
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in new command`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \newcommand{\test}{a_2}
                $\test$
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in math environments`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \[ a_b \]
                $ a_b $
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in label`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \label{l_1}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in input-like commands`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \input{chapter_1}
                \ProvidesPackage{my_package}
                \ProvidesClass{my_class}
                \documentclass[my_option]{my_class}
                \usepackage[my_option]{my_package}
                \include{my_file}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in url environment`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \url{web_site}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in comment`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                % this is a comment _
                
                \begin{center}
                    text
                    % this is a comment with a _
                    text
                \end{center}
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character triggers no warning in math`() {
        myFixture.configureByText(
            LatexFileType,
            """
                \begin{equation}
                    \begin{array}{cc}
                        \theta_1 & \theta_2 \\
                        \theta_3 & \theta_4
                    \end{array}
                    \label{eq:my-equation}
                \end{equation}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }

    fun `test unescaped _ character quick fix`() {
        testQuickFix(
            """
            \begin{document}
                some _ text
            \end{document}
            """.trimIndent(),
            """
            \begin{document}
                some \_ text
            \end{document}
            """.trimIndent()
        )
    }

    fun `test unescaped _ character warnings in block environment`() {
        myFixture.configureByText(
            LatexFileType,
            """
            This block is not in math context.
            \begin{block}{block title}
                a<warning descr="Escape character \ expected">_</warning> underscore
            \end{block}
            
            The block inside the equation (and thus the contents sf the equation) are in math context.
            \begin{equation*}
                \B{A} =
                \begin{blockarray}{ccccc}
                    & 1 & 2 & & m \\
                  \begin{block}{c(cccc)}
                    1 & a_{1,1} & a_{1,2} & \cdots & a_{1,m} \\
                    2 & a_{2,1} & a_{2,2} & \cdots & a_{2,m} \\
                    & \vdots  & \vdots  & \ddots & \vdots \\
                    n & a_{n,1} & a_{n,2} & \cdots & a_{n,m} \\
                  \end{block}
                \end{blockarray}
            \end{equation*}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, false)
    }
}
