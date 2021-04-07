package nl.hannahsten.texifyidea.run.logtab

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexFileStack
import nl.hannahsten.texifyidea.run.latex.logtab.LatexLogMagicRegex.LINE_WIDTH

class LatexLogFileFinderTest : BasePlatformTestCase() {

    fun testFileIsImmediatelyClosed() {
        val line =
            """(/home/abby/texlive/2019/texmf-dist/tex/latex/graphics/keyval.sty)"""
        // main.tex is on the top of the stack.
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testFileImmediatelyClosedMoreText() {
        val line =
            """(/home/abby/texlive/2019/texmf-dist/tex/latex/base/size10.clo)Latexmk: This is Latexmk, John Collins, 18 June 2019, version: 4.65."""
        val stack = LatexFileStack("/home/abby/texlive/2019/texmf-dist/tex/latex/base/article.cls", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("/home/abby/texlive/2019/texmf-dist/tex/latex/base/article.cls", newStack.peek())
    }

    fun testWindowsEvilPath() {
        val line =
            """("C:\Users\thomas\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\lstmisc.sty""""
        var stack = LatexFileStack("./main.tex")
        line.chunked(LINE_WIDTH).forEach {
            stack = stack.update(it)
        }
        assertEquals("""C:\Users\thomas\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\lstmisc.sty""", stack.peek())
    }

    fun testNewFile() {
        val line =
            """(./bloop.tex"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./bloop.tex", newStack.peek())
    }

    fun testFileClosing() {
        val line =
            """(./bloop.tex))"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./nested/test.tex", newStack.peek())
    }

    fun testFileStartOfLineClosing() {
        val line =
            """)"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./nested/test.tex", newStack.peek())
    }

    fun testNonFileParenthesis() {
        val line =
            """(see the transcript file for additional information)"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testFakePars() {
        val line =
            """This is pdfTeX, Version 3.14159265-2.6-1.40.20 (TeX Live 2019) (preloaded format=pdflatex)"""
        val stack = LatexFileStack()
        val newStack = stack.update(line)
        assertEquals(0, newStack.notClosedNonFileOpenParentheses)
    }

    fun testFakeParsWithExtraOpenPar() {
        val line =
            """This is pdfTeX, Version 3.14159265-2.6-1.40.20 (TeX Live 2019) (preloaded format=pdflatex) plus a ("""
        val stack = LatexFileStack()
        val newStack = stack.update(line)
        assertEquals(1, newStack.notClosedNonFileOpenParentheses)
    }

    fun testFileAndNonFileParenthesis() {
        val line =
            """) [2] (/home/abby/Documents/texify-test/out/main.aux)"""
        val stack = LatexFileStack("./lipsum.tex", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testParFun() {
        val line =
            """) ((try this)))"""
        val stack = LatexFileStack("./lipsum.tex", "./second.tex", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testFileExtensions() {
        val line =
            """(./src/_minted-thesis/F21236103977357A063E148CA83348D21F2D8067E0A256B6FCF34360A44AFD35.pygtex"""
        var stack = LatexFileStack("./lipsum.tex", "./main.tex")
        line.chunked(LINE_WIDTH).forEach {
            stack = stack.update(it)
        }
        assertEquals("""./src/_minted-thesis/F21236103977357A063E148CA83348D21F2D8067E0A256B6FCF34360A44AFD35.pygtex""", stack.peek())
    }

    fun testExtraClosingParenthesis() {
        val text =
            """
            ! Undefined control sequence.
            l.1229 ...canonical \) in \( \SO(8) \) is \( \Spin
                                                              (7) \), see~\cite[Theorem~...
            The control sequence at the end of the top line
            of your error message was never \def'ed. If you have
            misspelled it (e.g., `\hobx'), type `I' and the correct
            spelling (e.g., `I\hbox'). Otherwise just continue,
            and I'll forget about whatever was undefined.
            """.trimIndent()
        var stack = LatexFileStack()
        for (line in text.split('\n')) {
            line.chunked(LINE_WIDTH).forEach {
                stack = stack.update(it)
            }
        }
    }

    fun testFileMultipleLines() {
        val line =
            """) ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/psnfss\t1phv.fd""""
        var stack = LatexFileStack("./lipsum.tex", "./main.tex")
        line.chunked(LINE_WIDTH).forEach {
            stack = stack.update(it)
        }
        assertEquals("""C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/psnfss\t1phv.fd""", stack.peek())
    }

    fun testSingleFile() {
        val log =
            """
            ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
            thfunctions.base.code.tex")
            ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
            thfunctions.round.code.tex")
            ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
            thfunctions.misc.code.tex")
            
            ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/latex/pgf/math\pgfmath
            .sty"
            ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/utilities\
            pgfkeys.code.tex"
            ("C:\Users\thoscho\AppData\Local\Programs\MiKTeX 2.9\tex/generic/pgf/math\pgfma
            thfloat.code.tex")))
            """.trimIndent()
        var stack = LatexFileStack()
        log.split('\n').forEach {
            stack = stack.update(it + "\n")
        }
        assertTrue(stack.isEmpty())
    }

    fun testFileEndingHalfwayOpen() {
        val log =
            """
            (/usr/local/texlive/2020/texmf-dist/tex/latex/glossaries/base/glossaries.sty
            (/usr/local/texlive/2020/texmf-dist/tex/latex/glossaries/styles/glossary-super.
            sty (/usr/local/texlive/2020/texmf-dist/tex/latex/supertabular/supertabular.sty
            ))
            (/usr/local/texlive/2020/texmf-dist/tex/latex/glossaries/styles/glossary-tree.s
            ty))
            """.trimIndent()
        var stack = LatexFileStack()
        log.split('\n').forEach {
            stack = stack.update(it + "\n")
        }
        assertTrue(stack.isEmpty())
    }

    fun testFileEndingHalfwayOpen2() {
        val log =
            """
(/usr/local/texlive/2020/texmf-dist/tex/latex/pgf/basiclayer/pgfcore.sty
(/usr/local/texlive/2020/texmf-dist/tex/latex/pgf/systemlayer/pgfsys.sty
(/usr/local/texlive/2020/texmf-dist/tex/generic/pgf/systemlayer/pgfsys.code.tex
(/usr/local/texlive/2020/texmf-dist/tex/generic/pgf/utilities/pgfkeys.code.tex
(/usr/local/texlive/2020/texmf-dist/tex/generic/pgf/utilities/pgfkeysfiltered.c
ode.tex))
(/usr/local/texlive/2020/texmf-dist/tex/generic/pgf/systemlayer/pgf.cfg)
(/usr/local/texlive/2020/texmf-dist/tex/generic/pgf/systemlayer/pgfsys-pdftex.d
ef) )))
            """.trimIndent()
        var stack = LatexFileStack()
        log.split('\n').forEach {
            stack = stack.update(it + "\n")
        }
        assertTrue(stack.isEmpty())
    }

    fun testOverfullHboxWithPar() {
        val log =
            """
                
(/home/thomas/GitRepos/2mmc10-homework/out/homework6.sagetex.scmd
consecutive:
)
Overfull \hbox (31.54913pt too wide) in paragraph at lines 50--50
 []                         \OT1/lmtt/m/n/10 mod((x_1 * y_2 + y_1 * x_2)/(1 - 5
 * x_1 * x_2 * y_1 * y_2), p),[] 

Overfull \hbox (26.29915pt too wide) in paragraph at lines 51--51
 []                         \OT1/lmtt/m/n/10 mod((y_1 * y_2 - x_1 * x_2)/(1 - 5
 * x_1 * x_2 * y_1 * y_2), p)[] 
(/home/thomas/GitRepos/2mmc10-homework/out/homework6.sagetex.scmd
consecutive:
) (/home/thomas/GitRepos/2mmc10-homework/out/homework6.sagetex.scmd
consecutive:
)
            """.trimIndent()
        var stack = LatexFileStack()
        log.split('\n').forEach {
            stack = stack.update(it + "\n")
        }
        assertTrue(stack.isEmpty())
    }
}