package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.inspection.grammar.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.remote.GrazieRemote
import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.util.messages.Topic
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.util.parser.findFirstChildOfType

class GrazieInspectionTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "test/resources/inspections/grazie"

    companion object {
        @Volatile
        private var germanSetupDone = false
    }

    override fun setUp() {
        super.setUp()
        (myFixture as? CodeInsightTestFixtureImpl)?.canChangeDocumentDuringHighlighting(true)

        setupLanguages()

        myFixture.enableInspections(GrazieInspection(), GrazieSpellCheckingInspection(), GrazieInspection.Grammar(), GrazieInspection.Style())

        // Reset rules to default for each test
        updateConfig { it.copy(userEnabledRules = emptySet()) }
    }

    private fun waitForGrazie() {
        val topic = Topic(GrazieStateLifecycle::class.java)
        var tries = 0
        while (tries < 10) {
            if (ApplicationManager.getApplication().messageBus.hasUndeliveredEvents(topic)) {
                tries = 0
                Thread.sleep(100)
            }
            else {
                tries++
                Thread.sleep(100)
            }
        }
    }

    private fun updateConfig(change: (GrazieConfig.State) -> GrazieConfig.State) {
        GrazieConfig.update(change)
        waitForGrazie()
    }

    private fun setupLanguages() {
        if (germanSetupDone) return
        synchronized(GrazieInspectionTest::class.java) {
            if (germanSetupDone) return

            repeat(3) { attempt ->
                try {
                    GrazieRemote.downloadWithoutLicenseCheck(Lang.GERMANY_GERMAN)
                    // Try to access it to ensure it's loaded
                    Lang.GERMANY_GERMAN.jLanguage
                    return@repeat
                }
                catch (e: Throwable) {
                    if (attempt == 2) {
                        System.err.println("Failed to setup German after 3 attempts: ${e.message}")
                        e.printStackTrace()
                    }
                    Thread.sleep(2000)
                }
            }

            updateConfig { state ->
                val langs = state.enabledLanguages + Lang.GERMANY_GERMAN + Lang.AMERICAN_ENGLISH
                state.copy(enabledLanguages = langs)
            }

            // Force load in this thread to avoid CME during inspections
            Lang.AMERICAN_ENGLISH.jLanguage
            Lang.GERMANY_GERMAN.jLanguage

            germanSetupDone = true
        }
    }

    fun testSingleSentence() {
        myFixture.configureByText(LatexFileType, """Is these an error with a sentence $\xi$ end or not.""")
        myFixture.checkHighlighting()
    }

    fun testCommentInText() {
        myFixture.configureByText(
            LatexFileType,
            """
                \begin{document}
                    All <GRAMMAR_ERROR descr="The verb 'is' is singular. Did you mean: 'this is' or 'those are'?">those is</GRAMMAR_ERROR> problems in the middle of a sentence.
                    % <GRAMMAR_ERROR descr="The verb 'is' is singular. Did you mean: 'this is' or 'Those are'?">Those is</GRAMMAR_ERROR> a problem in a comment
                    <GRAMMAR_ERROR descr="The verb 'is' is singular. Did you mean: 'this is' or 'Those are'?">Those is</GRAMMAR_ERROR> a problem at the beginning of a sentence.
                \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, true)
    }

    fun testSentenceAtEnvironmentStart() {
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                <GRAMMAR_ERROR descr="Use 'An' instead of 'A' if the following word starts with a vowel sound, e.g. 'an article', 'an hour'.">A</GRAMMAR_ERROR> apple a day keeps the doctor away.
                Some other sentence.
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false, true)
    }

    fun testInlineMath() {
        myFixture.configureByText(LatexFileType, $$"""Does Grazie detect $m$ as a sentence?""")
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
        myFixture.configureByText(LatexFileType, """A sentence (in this case). More sentence.""")
        myFixture.checkHighlighting()
    }

    fun testGraphicsPath() {
        // graphicspath argument should be ignored, but it's not direct parameter text since it's in a group
        myFixture.configureByText(LatexFileType, """\graphicspath{{a apple}} is a sentence.""")
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
        myFixture.configureByText(
            LatexFileType,
            """
            \begin{document}
                Das ist eine Function $ f$.
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
        myFixture.configureByText(
            LatexFileType,
            $$"""
            Eine \textbf{Folge oder Zahlenfolge} in $M$ ist eine Abbildung
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testGermanGlossaries() {
        myFixture.configureByText(
            LatexFileType,
            """
            Der Hintergrund des Themas der Thesis ist der Umbruch beim Prozess des \gls{api}-Managements.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    fun testTabular() {
        myFixture.configureByText(
            LatexFileType,
            $$"""
                \begin{tabular}{llll}
                    $a$:                 & $\mathbb{N}$ & \rightarrow & $M$     \\
                    \multicolumn{1}{l}{} & $n$          & \mapsto     & $a(n)$.
                \end{tabular}

                Ich bin über die Entwicklung sehr froh.
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }

    /*
     * These rules are not enabled by default in Grazie Lite, but do show up by default in Grazie Pro.
     * To find a rule id, search for the name in https://community.languagetool.org/rule/list and use the id together with the prefex from LangTool.globalIdPrefix
     */

    fun testCommaInSentence() {
        updateConfig { it.copy(userEnabledRules = setOf("LanguageTool.EN.COMMA_PARENTHESIS_WHITESPACE")) }
        myFixture.configureByText(LatexFileType, """\label{fig} Similar to the structure presented in \autoref{fig}, it is.""")
        myFixture.checkHighlighting()
    }

    fun testCommandsInSentence() {
        updateConfig { it.copy(userEnabledRules = setOf("LanguageTool.EN.CONSECUTIVE_SPACES")) }
        myFixture.configureByText(LatexFileType, """The principles of a generic \ac{PID} controller.""")
        myFixture.checkHighlighting()
    }

    fun testCustomCommand() {
        updateConfig { it.copy(userEnabledRules = setOf("LanguageTool.EN.DT_JJ_NO_NOUN", "LanguageTool.EN.DT_JJ_NO_NOUN [1]", "LanguageTool.EN.DT_JJ_NO_NOUN [2]", "LanguageTool.EN.MISSING_NOUN [1]", "LanguageTool.EN.MISSING_NOUN")) }
        myFixture.configureByText(
            LatexFileType,
            """
                \documentclass[11pt]{article}
                \newcommand{\TIdVar}{\texttt{ThingIdVars}}
                \begin{document}
                    I need to get this for the \TIdVar{} to fix something.
                \end{document} 
            """.trimIndent()
        )
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
    private fun getSubmittedText(file: PsiFile): String = LatexTextExtractor().buildTextContent(file.findFirstChildOfType(LatexContent::class)!!).toString()

    fun testNewlinesShouldBeKept() {
        val text = """
            \section{First}
            \section{Second}
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        val submittedText = getSubmittedText(myFixture.file)
        assertEquals(
            """
            First
            Second
            """.trimIndent(),
            submittedText
        )
    }
}