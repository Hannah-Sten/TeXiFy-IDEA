package nl.hannahsten.texifyidea.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import nl.hannahsten.texifyidea.editor.pasteproviders.HtmlPasteProvider
import nl.hannahsten.texifyidea.file.LatexFile
import org.jsoup.Jsoup

class HtmlPasteProviderTest : BasePlatformTestCase() {

    fun testItalic() {
        myFixture.configureByText("main.tex", "")
        val html = "<i>italic</i>"
        val node = Jsoup.parse(html).select("body")[0]
        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile, canUseExternalTools = false)
        TestCase.assertEquals("\\textit{italic}", latex)
    }

}