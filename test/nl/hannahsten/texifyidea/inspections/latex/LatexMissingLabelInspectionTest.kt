package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.CommandManager
import nl.hannahsten.texifyidea.testutils.writeCommand
import org.junit.Test

class LatexMissingLabelInspectionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "test/resources/inspections/latex/missinglabel"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LatexMissingLabelInspection())
    }

    @Test
    fun testMissingLabelWarnings() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun testMissingFigureLabelWarnings() {
        myFixture.configureByText(LatexFileType, """
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
        """.trimIndent())
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun testMissingSectionLabelWarnings() {
        myFixture.configureByText(LatexFileType, """
            \newcommand{\mylabels}[2]{\section{#1}\label{sec:#2}}
            \newcommand{\mylabel}[1]{\label{sec:#1}}

            \section{some sec}\mylabel{sec:some-sec}
        """.trimIndent())
        CommandManager.updateAliases(setOf("\\label"), project)

        myFixture.checkHighlighting(false, false, true, false)
    }

    fun testMissingFigureLabelQuickFixWithCaption() {
        testQuickFix("""
            \begin{document}
                \begin{figure}
                    \caption{Some Caption}
                \end{figure}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \begin{figure}
                    \caption{Some Caption}\label{fig:figure}<caret>
                \end{figure}
            \end{document}
        """.trimIndent())
    }

    fun testMissingFigureLabelQuickFix() {
        testQuickFix("""
            \begin{document}
                \begin{figure}
            
                \end{figure}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \begin{figure}
                    \label{fig:figure}<caret>
            
                \end{figure}
            \end{document}
        """.trimIndent())
    }

    fun testMissingListingLabelWarnings() {
        myFixture.configureByText(LatexFileType, """
            \usepackage{listings}
            \begin{document}
                <weak_warning descr="Missing label">\begin{lstlisting}
                \end{lstlisting}</weak_warning>
                
                \begin{lstlisting}[label=somelabel]
                \end{lstlisting}
                
                \begin{lstlisting}[label={label with spaces}]
                \end{lstlisting}
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(false, false, true, false)
    }

    fun testListingLabelIsNotMissingWarnings() {
        myFixture.configureByText(LatexFileType, """
            \usepackage{listings}
            \begin{document}
                \begin{lstlisting}[language=Python, label=somelabel]
                \end{lstlisting}
                
                \begin{lstlisting}[label={label with spaces}]
                \end{lstlisting}
            \end{document}
        """.trimIndent())
        myFixture.checkHighlighting(false, false, true, false)
    }

    @Test
    fun testMissingListingLabelQuickFixNoParameters() {
        testQuickFix("""
            \begin{document}
                \begin{lstlisting}
                \end{lstlisting}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \begin{lstlisting}[label={lst:lstlisting}<caret>]
                \end{lstlisting}
            \end{document}
        """.trimIndent())
    }

    @Test
    fun testMissingListingLabelQuickFixExistingLabel() {
        testQuickFix("""
            \begin{document}
                \label{lst:lstlisting}
                \begin{lstlisting}
                \end{lstlisting}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \label{lst:lstlisting}
                \begin{lstlisting}[label={lst:lstlisting2}<caret>]
                \end{lstlisting}
            \end{document}
        """.trimIndent())
    }

    @Test
    fun testMissingListingLabelQuickFixExistingParameters() {
        testQuickFix("""
            \begin{document}
                \begin{lstlisting}[someoption,otheroption={with value}]
                \end{lstlisting}
            \end{document}
        """.trimIndent(), """
            \begin{document}
                \begin{lstlisting}[someoption,otheroption={with value},label={lst:lstlisting}<caret>]
                \end{lstlisting}
            \end{document}
        """.trimIndent())
    }

    private fun testQuickFix(before: String, after: String) {
        myFixture.configureByText(LatexFileType, before)
        val quickFixes = myFixture.getAllQuickFixes()
        writeCommand(myFixture.project) {
            quickFixes.first().invoke(myFixture.project, myFixture.editor, myFixture.file)
        }

        myFixture.checkResult(after)
    }
}