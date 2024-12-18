package nl.hannahsten.texifyidea.structure.latex

import nl.hannahsten.texifyidea.util.toRoman
import java.util.*
import kotlin.math.max

/**
 * @author Hannah Schellekens
 */
class SectionNumbering(private val documentClass: DocumentClass) {

    /**
     * 0: Part
     * 1: Chapter
     * 2: Section
     * 3: Subsection
     * 4: Subsubsection
     * 5: Paragraph
     * 6: Subparagraph
     */
    private val counters = IntArray(7)

    fun increase(level: Int) {
        counters[level]++

        // Parts don't reset other counters.
        if (level == 0) {
            return
        }

        for (i in level + 1 until counters.size) {
            counters[i] = 0
        }
    }

    fun setCounter(level: Int, amount: Int) {
        counters[level] = amount
    }

    fun addCounter(level: Int, amount: Int) {
        counters[level] += amount
    }

    fun getTitle(level: Int): String {
        // Parts
        if (level == 0) {
            return max(0, counters[0]).toRoman()
        }

        val sb = StringBuilder()
        var delimiter = ""

        for (i in documentClass.startIndex..level) {
            sb.append(delimiter)
            sb.append(counters[i])
            delimiter = "."
        }

        return sb.toString()
    }

    enum class DocumentClass(val startIndex: Int) {

        BOOK(1),
        ARTICLE(2);

        val className: String
            get() = name.lowercase(Locale.getDefault())

        companion object {

            fun getClassByName(name: String): DocumentClass {
                return if (BOOK.name == name) {
                    BOOK
                }
                else ARTICLE
            }
        }
    }
}
