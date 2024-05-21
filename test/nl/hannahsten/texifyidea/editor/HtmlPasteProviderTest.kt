package nl.hannahsten.texifyidea.editor

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import nl.hannahsten.texifyidea.editor.pasteproviders.HtmlPasteProvider
import nl.hannahsten.texifyidea.file.LatexFile
import org.jsoup.Jsoup

class HtmlPasteProviderTest : BasePlatformTestCase() {

    // Uses UI elements so cannot be run
//    fun testTable() {
//        myFixture.configureByText("main.tex", "")
//        val html = """
//            <body>
//            <table cellspacing="0" border="0">
//            	<colgroup span="2" width="85"></colgroup>
//            	<tr>
//            		<td height="17" align="left" valign=bottom>Column 1</td>
//            		<td align="left" valign=bottom>Column 2</td>
//            	</tr>
//            	<tr>
//            		<td height="17" align="left" valign=bottom>ABBR</td>
//            		<td align="right" valign=bottom sdval="1.27" sdnum="1043;">1</td>
//            	</tr>
//            	<tr>
//            		<td height="17" align="left" valign=bottom>DPTP</td>
//            		<td align="right" valign=bottom sdval="1,18" sdnum="1043;">1,18</td>
//            	</tr>
//            </table>
//            </body>
//        """.trimIndent()
//        val node = Jsoup.parse(html).select("body")[0]
//        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile)
//        TestCase.assertEquals("""
//         \begin{table}
//            \centering
//            \begin{tabular}{ll}
//                \toprule
//                \textbf{Column 1} & \textbf{Column 2 cost} \\
//                \midrule
//                ABBR & 1.27 \\
//                DPTP & 1,18 \\
//                \bottomrule
//            \end{tabular}
//            \caption{}
//            \label{tab:}
//        \end{table}
//        """.trimIndent(), latex)
//    }

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

    fun testList() {
        myFixture.configureByText("main.tex", "")
        // Source: pdf
        val html = """
            <!-- Version:0.9 -->
            <html>
            <body>
            <!--StartFragment-->3. a list:
            - one,
            - two,
            <!--EndFragment-->
            </body>
            </html>
        """.trimIndent()
        val result = """
            
            3. a list:
            - one,
            - two,


        """.trimIndent()
        val node = Jsoup.parse(html).select("body")[0]
        val latex = HtmlPasteProvider().convertHtmlToLatex(node, myFixture.file as LatexFile)
        TestCase.assertEquals(result, latex)
    }
}