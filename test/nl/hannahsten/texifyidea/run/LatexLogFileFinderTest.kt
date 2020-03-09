package nl.hannahsten.texifyidea.run

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.hannahsten.texifyidea.run.latex.logtab.LatexFileStack

class LatexLogFileFinderTest : BasePlatformTestCase() {
    fun testFileIsImmediatelyClosed() {
        val line = """(/home/abby/texlive/2019/texmf-dist/tex/latex/graphics/keyval.sty)"""
        // First main is pushed on the stack, then test.
        val stack = LatexFileStack("./main.tex", "./nested/test.tex")
        val newStack = stack.update(line)
        assertEquals("./main.tex", newStack.peek())
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
}