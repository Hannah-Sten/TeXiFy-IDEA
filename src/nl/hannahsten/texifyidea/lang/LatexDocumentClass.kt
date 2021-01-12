package nl.hannahsten.texifyidea.lang

/**
 * All LaTeX document classes for which TeXiFy has special behaviour.
 *
 * @author Thomas
 */
open class LatexDocumentClass(
    val name: String,
    vararg val parameters: String = emptyArray()
) {

    companion object {

        val EXAM = LatexDocumentClass("exam")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is LatexDocumentClass) {
            return false
        }

        val aPackage = other as LatexDocumentClass?
        return name == aPackage!!.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "DocumentClass{$name}"
}