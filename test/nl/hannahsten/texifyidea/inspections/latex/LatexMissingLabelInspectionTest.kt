package nl.hannahsten.texifyidea.inspections.latex

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.intentions.LatexAddLabelIntention
import nl.hannahsten.texifyidea.lang.CommandManager

class LatexMissingLabelInspectionTest : TexifyInspectionTestBase(LatexMissingLabelInspection()) {
    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/missinglabel"
    }

    fun `test missing label warnings`() {
        myFixture.configureByFile("MissingLabelWarnings.tex")
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun `test missing figure label warnings`() = testHighlighting(
            """
            \begin{document}
                % figure without label
                <weak_warning descr="Missing label">\begin{figure}
                \end{figure}</weak_warning>
    
                % figure with label
                \begin{figure}
                    \label{fig:figure-label}
                \end{figure}
    
                % figure with label in caption
                \begin{figure}
                    \caption{Some text \label{fig:figure-caption-label}}
                \end{figure}
            \end{document}
            """.trimIndent()
        )

    fun `test missing section label no warnings (custom label command)`() {
        myFixture.configureByText(
            LatexFileType,
            """
            \newcommand{\mylabels}[2]{\section{#1}\label{sec:#2}}
            \newcommand{\mylabel}[1]{\label{sec:#1}}

            \section{some sec}\mylabel{some-sec}
            """.trimIndent()
        )
        CommandManager.updateAliases(setOf("\\label"), project)

        myFixture.checkHighlighting(false, false, true, false)
    }

    fun `test quick fix in figure with caption`() = testQuickFix(
        before = """
        \begin{document}
            \begin{figure}
                \caption{Some Caption}
            \end{figure}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
            \begin{figure}
                \caption{Some Caption}\label{fig:figure}<caret>
            \end{figure}
        \end{document}
        """.trimIndent()
    )

    fun `test quick fix in figure`() = testQuickFix(
        before = """
        \begin{document}
            \begin{figure}
        
            \end{figure}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
            \begin{figure}
                \label{fig:figure}<caret>
        
            \end{figure}
        \end{document}
        """.trimIndent()
    )

    fun `test missing listings label warnings`() = testHighlighting(
            """
            \usepackage{listings}
            \begin{document}
                <weak_warning descr="Missing label">\begin{lstlisting}
                \end{lstlisting}</weak_warning>
                
                \begin{lstlisting}[label=somelabel]
                \end{lstlisting}
                
                \begin{lstlisting}[label={label with spaces}]
                \end{lstlisting}
            \end{document}
            """.trimIndent()
        )

    fun `test listings label no warnings`() = testHighlighting(
            """
            \usepackage{listings}
            \begin{document}
                \begin{lstlisting}[language=Python, label=somelabel]
                \end{lstlisting}
                
                \begin{lstlisting}[label={label with spaces}]
                \end{lstlisting}
            \end{document}
            """.trimIndent()
        )

    fun `test quick fix in listings with no other parameters`() = testQuickFix(
        before = """
        \begin{document}
            \begin{lstlisting}
            \end{lstlisting}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
            \begin{lstlisting}[label={lst:lstlisting}<caret>]
            \end{lstlisting}
        \end{document}
        """.trimIndent()
    )

    fun `test quick fix in listings when label already exists`() = testQuickFix(
        before = """
        \begin{document}
            \label{lst:lstlisting}
            \begin{lstlisting}
            \end{lstlisting}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
            \label{lst:lstlisting}
            \begin{lstlisting}[label={lst:lstlisting2}<caret>]
            \end{lstlisting}
        \end{document}
        """.trimIndent()
    )

    fun `test quick fix in listings with other parameters`() = testQuickFix(
        before = """
        \begin{document}
            \begin{lstlisting}[someoption,otheroption={with value}]
            \end{lstlisting}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
            \begin{lstlisting}[someoption,otheroption={with value},label={lst:lstlisting}<caret>]
            \end{lstlisting}
        \end{document}
        """.trimIndent()
    )
}