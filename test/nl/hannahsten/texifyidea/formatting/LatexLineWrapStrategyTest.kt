package nl.hannahsten.texifyidea.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.psi.codeStyle.CodeStyleSettingsManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.grammar.LatexLanguage

class LatexLineWrapStrategyTest : BasePlatformTestCase() {

    private fun setUpTest() {
        val settings = CodeStyle.createTestSettings(CodeStyle.getSettings(project))
        settings.defaultRightMargin = 30
        settings.getCommonSettings(LatexLanguage).WRAP_LONG_LINES = true
        CodeStyleSettingsManager.getInstance(project).setTemporarySettings(settings)
    }

    fun testCommentWrap() {
        setUpTest()
        val text = """
            %     https://Petra-indicates-clears-to relate the selection's systems.
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        myFixture.performEditorAction("ReformatCode")
        val expected = """
            %     https://Petra-indicates-clears-to
            % relate the selection's 
            % systems.
        """.trimIndent()
        myFixture.checkResult(expected)
    }

    fun testHrefWrap() {
        setUpTest()
        val text = """
            \section{This includes permissions.}
            \href{https://the-very-very-long.url}{This includes} permissions.
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        myFixture.performEditorAction("ReformatCode")
        val expected = """
            \section{This includes permissions.}
            \href{https://the-very-very-long.url}{This includes}
            permissions.
        """.trimIndent()
        myFixture.checkResult(expected)
    }

    fun testUrlWrap() {
        val text = """
            \href{https://the-very-very-long.url}{unreasonable long <caret>}
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        val settings = CodeStyle.createTestSettings(CodeStyle.getSettings(project))
        CodeStyleSettingsManager.getInstance(project).setTemporarySettings(settings)
        settings.defaultRightMargin = 30
        settings.WRAP_WHEN_TYPING_REACHES_RIGHT_MARGIN = true
        myFixture.type("URL")
        val expected = """
            \href{https://the-very-very-long.url}{unreasonable long URL}
        """.trimIndent()
        myFixture.checkResult(expected)
    }

    fun testTextWrap() {
        setUpTest()
        val text = """
        \documentclass{article}
        \begin{document}
            Über die grüne Wiese hüpft
            er das gemeinsame Frühstück
        
            Beisammensein, onders ${'$'}^{,}${'$'} bei
        
            jedoch \footfullcite{author} abhielt
        
        \end{document}
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        myFixture.performEditorAction("ReformatCode")
        val expected = """
        \documentclass{article}
        \begin{document}
            Über die grüne Wiese hüpft
            er das gemeinsame 
            Frühstück
        
            Beisammensein, onders 
            ${'$'}^{,}${'$'} bei
        
            jedoch 
            \footfullcite{author} 
            abhielt
        
        \end{document}
        """.trimIndent()
        myFixture.checkResult(expected)
    }

    fun testParenthesisWrap() {
        setUpTest()
        val text = """
            Lorem ipsum dolor amet (aaaaaaaa)
        """.trimIndent()
        myFixture.configureByText(LatexFileType, text)
        myFixture.performEditorAction("ReformatCode")
        val expected = """
            Lorem ipsum dolor amet
            (aaaaaaaa)
        """.trimIndent()
        myFixture.checkResult(expected)
    }
}