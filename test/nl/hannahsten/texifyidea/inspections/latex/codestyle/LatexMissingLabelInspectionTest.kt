package nl.hannahsten.texifyidea.inspections.latex.codestyle

import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.inspections.TexifyInspectionTestBase
import nl.hannahsten.texifyidea.lang.alias.CommandManager
import nl.hannahsten.texifyidea.psi.LatexKeyValuePair
import nl.hannahsten.texifyidea.settings.conventions.LabelConventionType
import nl.hannahsten.texifyidea.settings.conventions.TexifyConventionsScheme
import nl.hannahsten.texifyidea.testutils.updateConvention
import nl.hannahsten.texifyidea.util.parser.childrenOfType

class LatexMissingLabelInspectionTest : TexifyInspectionTestBase(LatexMissingLabelInspection()) {

    override fun setUp() {
        super.setUp()
        // reset to default
        myFixture.updateConvention { s -> s.currentScheme = TexifyConventionsScheme() }
    }

    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/missinglabel"
    }

    fun `test missing label warnings`() {
        myFixture.configureByFile("MissingLabelWarnings.tex")
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun `test no missing label warning if convention is disabled`() {
        myFixture.updateConvention { s ->
            s.getLabelConvention("\\section", LabelConventionType.COMMAND)!!.enabled = false
            s.getLabelConvention("figure", LabelConventionType.ENVIRONMENT)!!.enabled = false
        }
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                \section{some section}
                
                \begin{figure}
                \end{figure}
            \end{document}
            """.trimIndent()
        )

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

    fun `test label name generation`() = testQuickFix(
        before = """
        \section{~Ação –função}
        """.trimIndent(),
        after = """
        \section{~Ação –função}\label{sec:acao-funcao}
        """.trimIndent(),
        numberOfFixes = 2
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

    fun `test exam parts`() = testHighlighting(
        """
            \documentclass{exam}
            \begin{document}
                \begin{questions}
                    \question
                    \begin{parts}
                        \part a
                    \end{parts}
                \end{questions}
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
            \begin{lstlisting}[label={lst:lstlisting}]
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
            \begin{lstlisting}[label={lst:lstlisting2}]
            \end{lstlisting}
        \end{document}
        """.trimIndent()
    )

    fun `test quick fix in listings with other parameters`() {
        testQuickFix(
            before = """
                \begin{document}
                    \begin{lstlisting}[someoption,otheroption={with value}]
                    \end{lstlisting}
                \end{document}
            """.trimIndent(),
            after = """
                \begin{document}
                    \begin{lstlisting}[someoption,otheroption={with value},label={lst:lstlisting}]
                    \end{lstlisting}
                \end{document}
            """.trimIndent()
        )
        // Sometimes, errors in psi structure only show when initiating a WalkingState
        myFixture.file.children.first().childrenOfType(LatexKeyValuePair::class)
    }

    fun `test fix all missing label problems in this file`() = testQuickFixAll(
        before = """
                \section{one}
                \section{two}
        """.trimIndent(),
        after = """
                \section{one}\label{sec:one}
                
                
                \section{two}\label{sec:two}
        """.trimIndent(),
        quickFixName = "Add label for this command",
        numberOfFixes = 4
    )

    fun `test missing lstinputlistings label warnings`() = testHighlighting(
        """
            \usepackage{listings}
            \begin{document}
                <weak_warning descr="Missing label">\lstinputlisting{some/file}</weak_warning>
                
                \lstinputlisting[label={lst:inputlisting}]{some/file}
                
                \lstinputlisting[label={lst:inputlisting with spaces}]{some/file}
            \end{document}
        """.trimIndent()
    )

    fun `test lstinputlistings label no warnings`() = testHighlighting(
        """
            \usepackage{listings}
            \begin{document}
                \lstinputlisting[label={lst:inputlisting}]{some/file}
                
                \lstinputlisting[label={lst:inputlisting with spaces}]{some/file}
            \end{document}
        """.trimIndent()
    )

    fun `test quick fix in lstinputlistings with other parameters`() = testQuickFix(
        before = """
        \begin{document}
                \lstinputlisting[someoption,otheroption={with value}]{some/file}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
                \lstinputlisting[someoption,otheroption={with value},label={lst:lstinputlisting}]{some/file}
        \end{document}
        """.trimIndent()
    )

    fun `test quick fix in lstinputlistings creates optional parameters at correct position`() = testQuickFix(
        before = """
        \begin{document}
                \lstinputlisting{some/file}
        \end{document}
        """.trimIndent(),
        after = """
        \begin{document}
                \lstinputlisting[label={lst:lstinputlisting}]{some/file}
        \end{document}
        """.trimIndent()
    )
}