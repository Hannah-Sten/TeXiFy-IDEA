package nl.rubensten.texifyidea.intentions.latexmathtoggle

import org.junit.Test
import kotlin.test.assertEquals

class OneLinerTest {

    @Test
    fun oneLine() {
        val equation = "f(x, y) = 3x + y"
        assertEquals(equation, OneLiner(equation).getOneLiner())
    }

    @Test
    fun simpleAlign() {
        val equation = "f(x, y) &= 3x + y \\\\\n" +
                "&= 3x + 2y - y"
        val goal = "f(x, y) = 3x + y = 3x + 2y - y"
        assertEquals(goal, OneLiner(equation).getOneLiner())
    }

    @Test
    fun simpleMultiLine() {
        val equation = "f(x) = x^{32} + 2 x^{16} + 4 x^8 \\\\\n" +
                "+ 8 x^4 + 16 x^2 + 32 x"
        val goal = "f(x) = x^{32} + 2 x^{16} + 4 x^8 + 8 x^4 + 16 x^2 + 32 x"
        assertEquals(goal, OneLiner(equation).getOneLiner())
    }

    @Test
    fun casesOnly() {
        val equation = "\\begin{cases}\n" +
                "    1 & x < 0 \\\\\n" +
                "    0 & x \\ge 0\n" +
                "\\end{cases}"
        assertEquals(equation, OneLiner(equation).getOneLiner())
    }

    @Test
    fun casesInAlign() {
        val equation = "f(x) &= \\begin{cases}\n" +
                "            1 & x < 0 \\\\\n" +
                "            0 & x \\ge 0\n" +
                "\\end{cases} \\\\\n" +
                "&= -_-"
        val goal = "f(x) = \\begin{cases}\n" +
                "            1 & x < 0 \\\\\n" +
                "            0 & x \\ge 0\n" +
                "\\end{cases} = -_-"
        assertEquals(goal, OneLiner(equation).getOneLiner())
    }

    @Test
    fun multipleCases() {
        val equation = "f(x) &= \\begin{cases}\n" +
                "            1 & x < 0 \\\\\n" +
                "            0 & x \\ge 0\n" +
                "\\end{cases} \\\\\n" +
                "f(x) &= \\begin{cases}\n" +
                "            1 & x < 0 \\\\\n" +
                "            0 & x \\ge 0\n" +
                "\\end{cases}"
        val goal = "f(x) = \\begin{cases}\n" +
                "            1 & x < 0 \\\\\n" +
                "            0 & x \\ge 0\n" +
                "\\end{cases} f(x) = \\begin{cases}\n" +
                "            1 & x < 0 \\\\\n" +
                "            0 & x \\ge 0\n" +
                "\\end{cases}"
        assertEquals(goal, OneLiner(equation).getOneLiner())
    }

    @Test
    fun nestedCases() {
        val equation = "function = \\begin{cases} \n" +
                "               case1 &\\mbox{if } n = 0 \\\\\n" +
                "               \\begin{cases} \n" +
                "                   case2 &\\mbox{if } n = 1 \\\\\n" +
                "                   \\begin{cases} \n" +
                "                       case3 &\\mbox{if } n = 2 \\\\\n" +
                "                       case4 &\\mbox{if } n = 3 \n" +
                "                   \\end{cases}\n" +
                "               \\end{cases}\n" +
                "           \\end{cases}"
        assertEquals(equation, OneLiner(equation).getOneLiner())
    }

    @Test
    fun casesInSplit() {
        val equation = "\\begin{split}\\begin{cases}\n" +
                "    1 & x < 0 \\\\\n" +
                "    0 & x \\ge 0\n" +
                "\\end{cases}\\end{split}"
        assertEquals(equation, OneLiner(equation).getOneLiner())
    }

    @Test
    fun interText() {
        val equation = "x &= y \\\\\n" +
                "&= x \\\\\n" +
                "\\intertext{hello} \n" +
                "&= y"
        val goal = "x = y = x \\text{hello} = y"
        assertEquals(goal, OneLiner(equation).getOneLiner())
    }
}