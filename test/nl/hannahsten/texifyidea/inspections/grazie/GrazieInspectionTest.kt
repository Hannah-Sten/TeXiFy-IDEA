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

    // No idea why it doesn't work
//    fun testMultilineCheckGrammar() {
//        val testName = getTestName(false)
//        myFixture.configureByFile("$testName.tex")
//        myFixture.checkHighlighting(true, false, false, true)
//    }

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
            LatexFileType, """a (in this case) . aa"""
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

                Dies ist <warning descr="Möglicherweise fehlende grammatische Übereinstimmung von Kasus, Numerus oder Genus. Beispiel: 'mein kleiner Haus' statt 'mein kleines Haus'">eine deutscher Satz</warning>.% This comment is a sentence so should end with a full stop.
                Und hier ist ein zweiter Satz.\newline
                Und hier ist ein dritter Satz.
            \end{document}
            """.trimIndent()
        )
        myFixture.checkHighlighting()
    }
}