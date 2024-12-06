package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.inspection.grammar.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.remote.GrazieRemote
import com.intellij.openapi.application.ApplicationManager
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.util.messages.Topic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.LatexPsiHelper

class GrazieInspectionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String {
        return "test/resources/inspections/grazie"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(GrazieInspection(), SpellCheckingInspection())
        (myFixture as? CodeInsightTestFixtureImpl)?.canChangeDocumentDuringHighlighting(true)

        while (ApplicationManager.getApplication().messageBus.hasUndeliveredEvents(Topic(GrazieStateLifecycle::class.java))) {
            Thread.sleep(100)
        }
    }

    fun testCheckGrammarInConstructs() {
        myFixture.configureByText(LatexFileType, """Is these an error with a sentence ${'$'}\xi${'$'} end or not.""")
        myFixture.checkHighlighting()
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(true, false, false, true)
    }

    fun testMultilineCheckGrammar() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(true, false, false, true)
    }

    fun testInlineMath() {
        myFixture.configureByText(
            LatexFileType, """Does Grazie detect ${'$'}m$ as a sentence?"""
        )
        myFixture.checkHighlighting()
    }

    fun testSentenceStart() {
        myFixture.configureByText(
            LatexFileType,
            """
               \subsubsection{Something}
                The hardware requirements 
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testMatchingParens() {
        myFixture.configureByText(
            LatexFileType, """A sentence (in this case). More sentence."""
        )
        myFixture.checkHighlighting()
    }

    fun testUnpairedSymbol() {
        myFixture.configureByText(
            LatexFileType,
            """
                This is an unpaired symbol example
                with \textit{example text}. % Error at ending bracket
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testGerman() {
        GrazieRemote.download(Lang.GERMANY_GERMAN)
        GrazieConfig.update { it.copy(enabledLanguages = it.enabledLanguages + Lang.GERMANY_GERMAN) }
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                Das ist eine Function ${'$'} f${'$'}.
                Nur zum Testen.

                Dies ist <GRAMMAR_ERROR descr="Möglicherweise passen das Nomen und die Wörter, die das Nomen beschreiben, grammatisch nicht zusammen.">eine deutscher Satz</GRAMMAR_ERROR>.
                Und hier ist ein zweiter Satz.\newline
                Und hier ist ein dritter Satz.% This comment is a sentence so should end with a full stop.
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testGermanList() {
        GrazieRemote.download(Lang.GERMANY_GERMAN)
        GrazieConfig.update { it.copy(enabledLanguages = it.enabledLanguages + Lang.GERMANY_GERMAN) }
        myFixture.configureByText(
            LatexFileType,
            """
                \item Bietet es eine unterstützende Lösung?
                \item Können diese Dinge durchgeführt werden?
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testGermanCommandSpacing() {
        GrazieRemote.download(Lang.GERMANY_GERMAN)
        GrazieConfig.update { it.copy(enabledLanguages = it.enabledLanguages + Lang.GERMANY_GERMAN) }
        myFixture.configureByText(
            LatexFileType,
            """
            Eine \textbf{Folge oder Zahlenfolge} in ${'$'}M${'$'} ist eine Abbildung
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testTabular() {
        GrazieRemote.download(Lang.GERMANY_GERMAN)
        GrazieConfig.update { it.copy(enabledLanguages = it.enabledLanguages + Lang.GERMANY_GERMAN) }
        myFixture.configureByText(
            LatexFileType,
            """
                \begin{tabular}{llll}
                    ${'$'}a${'$'}:                 & ${'$'}\mathbb{N}${'$'} & \rightarrow & ${'$'}M${'$'}     \\
                    \multicolumn{1}{l}{} & ${'$'}n${'$'}          & \mapsto     & ${'$'}a(n)${'$'}.
                \end{tabular}

                Ich bin über die Entwicklung sehr froh.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    /*
     * These rules are not enabled by default in Grazie Lite, but do show up by default in Grazie Pro.
     */


    fun testCommaInSentence() {
        GrazieConfig.update { it.copy(userEnabledRules = setOf("COMMA_PARENTHESIS_WHITESPACE")) }
        myFixture.configureByText(LatexFileType, """\label{fig} Similar to the structure presented in \autoref{fig}, it is.""")
        myFixture.checkHighlighting()
    }


    fun testCommandsInSentence() {
        GrazieConfig.update { it.copy(userEnabledRules = setOf("CONSECUTIVE_SPACES")) }
        myFixture.configureByText(LatexFileType, """The principles of a generic \ac{PID} controller.""")
        myFixture.checkHighlighting()
    }

    /*
     * Grazie Pro
     *
     *  These tests only test false positives in Grazie Pro (com.intellij.grazie.pro.style.StyleInspection), but that is not possible to test at the moment: https://youtrack.jetbrains.com/issue/GRZ-5023
     * So we test the excluded ranges directly.
     */

    /**
     * Text as sent to Grazie.
     */
    fun getSubmittedText(rootText: String, ranges: List<IntRange>): String {
        return ranges.sortedBy { it.first }.flatMap { listOf(it.first, it.last) }.toMutableList().also { it.add(0, -1) }
            .chunked(2) { if (it.size > 1) rootText.substring(it[0] + 1, it[1]) else null }.joinToString()
    }

    fun testLongTextInCommand() {
        val text =  """
            \section{This is the first section of my test document}
            \section{The second section that is present in my testing document}
            \section{A third section that I also put in my test document to showcase this issue}
            \section{Here is a fourth section that I am putting in my document}
            """.trimIndent()

        myFixture.checkHighlighting()

        val file = LatexPsiHelper(myFixture.project).createFromText(text)
        val ranges = LatexTextExtractor().getStealthyRanges(file)
        val submittedText = getSubmittedText(text, ranges)
        assertEquals("A PID controller.", submittedText)
    }
}