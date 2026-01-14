package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase

class LatexGatherEquationsInspectionTest : TexifyInspectionTestBase(LatexGatherEquationsInspection()) {

    fun `test two consecutive display math environments`() {
        myFixture.configureByText(
            LatexFileType,
            """
            <weak_warning descr="Equations can be gathered">\[
                x = y
            \]</weak_warning>
            <weak_warning descr="Equations can be gathered">\[
                y = x
            \]</weak_warning>
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test two consecutive inline math environments`() {
        myFixture.configureByText(
            LatexFileType,
            $$"""
            $a=b$
            $b=a$
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test display math seperated by inline math environments`() {
        myFixture.configureByText(
            LatexFileType,
            $$"""
            \[
                a=b
            \]
            $c=d$
            \[
                e=f
            \]
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test two consecutive (non-display) math environments`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \[
                x = y
            \]
            \begin{equation}\label{eq:yx}
                y = x            
            \end{equation}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test two consecutive (non-display) math environments in item`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{enumerate}
                \item{\[a=b\]}
                \item{\[b=a\]}
            \end{enumerate}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun `test quick fix`() {
        testQuickFix(
            """
            \usepackage{amsmath}
            \begin{equation}\label{eq:xx}
                x = x
            \end{equation}
            \[
                a = b
            \]
            \[
                x = y
            \]
            \[
                y = x
            \]
            \begin{equation}\label{eq:yy}
                y = y
            \end{equation}
            """.trimIndent(),
            """
            \usepackage{amsmath}
            \begin{equation}\label{eq:xx}
                x = x
            \end{equation}
            \begin{gather*}
                a = b\\
                x = y\\
                y = x\\
            \end{gather*}
            \begin{equation}\label{eq:yy}
                y = y
            \end{equation}
            """.trimIndent(),
            3,
            2,
            updateCommand = true
        )
    }
}