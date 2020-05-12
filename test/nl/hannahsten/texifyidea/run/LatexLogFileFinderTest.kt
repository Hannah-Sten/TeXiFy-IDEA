package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexFileStack

class LatexLogFileFinderTest : BasePlatformTestCase() {
    fun testFileIsImmediatelyClosed() {
        val line = """(/home/abby/texlive/2019/texmf-dist/tex/latex/graphics/keyval.sty)"""
        // main.tex is on the top of the stack.
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testFileImmediatelyClosedMoreText() {
        val line = """(/home/abby/texlive/2019/texmf-dist/tex/latex/base/size10.clo)Latexmk: This is Latexmk, John Collins, 18 June 2019, version: 4.65."""
        val stack = LatexFileStack("/home/abby/texlive/2019/texmf-dist/tex/latex/base/article.cls", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("/home/abby/texlive/2019/texmf-dist/tex/latex/base/article.cls", newStack.peek())
    }

    fun testWindowsEvilPath() {
        val line = """("C:\Users\thomas\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\lstmisc.sty""""
        val stack = LatexFileStack("./main.tex")
        val newStack = stack.update(line)
        assertEquals(""""C:\Users\thomas\AppData\Local\Programs\MiKTeX 2.9\tex/latex/listings\lstmisc.sty"""", newStack.peek())
    }

    fun testNewFile() {
        val line = """(./bloop.tex"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./bloop.tex", newStack.peek())
    }

    fun testFileClosing() {
        val line = """(./bloop.tex))"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./nested/test.tex", newStack.peek())
    }

    fun testFileStartOfLineClosing() {
        val line = """)"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./nested/test.tex", newStack.peek())
    }

    fun testNonFileParenthesis() {
        val line = """(see the transcript file for additional information)"""
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testFakePars() {
        val line = """This is pdfTeX, Version 3.14159265-2.6-1.40.20 (TeX Live 2019) (preloaded format=pdflatex)"""
        val stack = LatexFileStack()
        val newStack = stack.update(line)
        assertEquals(0, newStack.nonFileParCount)
    }

    fun testFileAndNonFileParenthesis() {
        val line = """) [2] (/home/abby/Documents/texify-test/out/main.aux)"""
        val stack = LatexFileStack("./lipsum.tex", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testParFun() {
        val line = """) ((try this)))"""
        val stack = LatexFileStack("./lipsum.tex", "./second.tex", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
    }

    fun testFileExtensions() {
        val line ="""(./src/_minted-thesis/F21236103977357A063E148CA83348D21F2D8067E0A256B6FCF34360A44AFD35.pygtex"""
        val stack = LatexFileStack("./lipsum.tex", "./main.tex")
        val newStack = stack.update(line)
        assertEquals("""./src/_minted-thesis/F21236103977357A063E148CA83348D21F2D8067E0A256B6FCF34360A44AFD35.pygtex""", newStack.peek())
    }
}