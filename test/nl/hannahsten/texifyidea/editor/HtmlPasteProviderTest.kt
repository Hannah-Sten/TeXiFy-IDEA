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
        // Source: github
        val html = """
            <pre class="notranslate"><span class="pl-k">\newcommand</span>{<span class="pl-c1">\mylabel</span>}[1]{<span class="pl-k">\label</span>{#<span class="pl-v">1</span>}}

            <span class="pl-c1">\section</span>{<span class="pl-en">One</span>}<span class="pl-c1">\mylabel</span>{sec:one}</pre>
        """.trimIndent()
        val result = """
            \newcommand{\mylabel}[1]{\label{#1}}

            \section{One}\mylabel{sec:one}
        """.trimIndent()
        val node = Jsoup.parse(html).select("body")[0]
        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile)
        TestCase.assertEquals(result, latex)
    }
}