package nl.hannahsten.texifyidea.inspections.grazie

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.jlanguage.Lang
import com.intellij.openapi.application.ApplicationManager
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import org.junit.Test

class GrazieInspectionTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testData/inspections/grazie"
    }

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(GrazieInspection(), SpellCheckingInspection())
        (myFixture as? CodeInsightTestFixtureImpl)?.canChangeDocumentDuringHighlighting(true)

        GrazieConfig.update { state ->
            state.update(enabledLanguages = Lang.values().toSet())
        }

        while (ApplicationManager.getApplication().messageBus.hasUndeliveredEvents(GrazieStateLifecycle.topic)) {
            Thread.sleep(100)
        }
    }

    @Test
    fun testCheckGrammarInConstructs() {
        val testName = getTestName(false)
        myFixture.configureByFile("$testName.tex")
        myFixture.checkHighlighting(true, false, false, true)

    }
}