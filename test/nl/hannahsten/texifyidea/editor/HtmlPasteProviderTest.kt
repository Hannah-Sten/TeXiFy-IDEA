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
        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile)
        TestCase.assertEquals("\\textit{italic}", latex)
    }

    fun testLatex() {
        myFixture.configureByText("main.tex", "")
        val html = "\\canpaste{\\LaTex}"
        val node = Jsoup.parse(html).select("body")[0]
        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile)
        TestCase.assertEquals("\\canpaste{\\LaTex}", latex)
    }

    fun testNewlines() {
        myFixture.configureByText("main.tex", "")
        val html = """
            \newcommand{\mylabel}[1]{\label{#1}}

            \section{One}\mylabel{sec:one}
        """.trimIndent()
        val node = Jsoup.parse(html).select("body")[0]
        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile)
        TestCase.assertEquals(html, latex)
    }
}