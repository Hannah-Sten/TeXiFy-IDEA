package nl.hannahsten.texifyidea.run.latex.logtab

import java.util.*

class LatexFileStack : ArrayDeque<String>() {
    override fun peek(): String? = try {
        super.peek()
    }
    catch (e: IndexOutOfBoundsException) {
        null
    }
}