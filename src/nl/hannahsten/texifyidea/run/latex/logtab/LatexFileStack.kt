package nl.hannahsten.texifyidea.run.latex.logtab

import java.util.*

class LatexFileStack : ArrayDeque<String>() {
    override fun peek(): String? = if (isEmpty()) null else super.peek()
}