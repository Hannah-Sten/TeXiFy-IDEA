package nl.hannahsten.texifyidea.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.updateFilesets

class LatexQuoteInsertHandlerTest : BasePlatformTestCase() {

    fun testCsquotes() {
        myFixture.configureByText(LatexFileType, """Typing <caret> test""")
        TexifySettings.getState().automaticQuoteReplacement = TexifySettings.QuoteReplacement.CSQUOTES
        myFixture.type("\"")
        myFixture.checkResult("""\usepackage{csquotes}Typing \enquote{<caret>} test""")
    }
    fun testCsquotes2() {
        myFixture.configureByText(LatexFileType, """\usepackage{csquotes}Typing \enquote{<caret>} test""")
        TexifySettings.getState().automaticQuoteReplacement = TexifySettings.QuoteReplacement.CSQUOTES
        myFixture.updateFilesets()
        myFixture.type("quote\"")
        myFixture.checkResult("""\usepackage{csquotes}Typing \enquote{quote}<caret> test""")
    }

    fun testCsquotesInWord() {
        myFixture.configureByText(LatexFileType, """Typ<caret>ing test""")
        TexifySettings.getState().automaticQuoteReplacement = TexifySettings.QuoteReplacement.CSQUOTES
        myFixture.type("\"")
        myFixture.checkResult("""\usepackage{csquotes}Typ\enquote{<caret>}ing test""")
    }

    fun testCsquotesEndOfLine() {
        myFixture.configureByText(LatexFileType, """Typing <caret>""")
        TexifySettings.getState().automaticQuoteReplacement = TexifySettings.QuoteReplacement.CSQUOTES
        myFixture.type("\"")
        myFixture.checkResult("""\usepackage{csquotes}Typing \enquote{<caret>}""")
    }

    fun testEscapedQuotes() {
        myFixture.configureByText(LatexFileType, """Typing \<caret>""")
        TexifySettings.getState().automaticQuoteReplacement = TexifySettings.QuoteReplacement.LIGATURES
        myFixture.type("\"")
        myFixture.checkResult("""Typing \"""")
    }

    fun `test no replacement of single quote in word`() {
        myFixture.configureByText(LatexFileType, """Typing John<caret>""")
        TexifySettings.getState().automaticQuoteReplacement = TexifySettings.QuoteReplacement.CSQUOTES
        myFixture.type('\'')
        myFixture.checkResult("""Typing John'""")
    }
}