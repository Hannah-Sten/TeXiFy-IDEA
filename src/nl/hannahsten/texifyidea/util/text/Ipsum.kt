package nl.hannahsten.texifyidea.util.text

import nl.hannahsten.texifyidea.lang.Described

/**
 * @author Hannah Schellekens
 */
enum class Ipsum(override val description: String, val generateInput: () -> String) : Described {

    TEXIFY_IDEA_IPSUM("TeXiFy IDEA Ipsum", "/nl/hannahsten/texifyidea/ipsum/texify.txt".classpathReader()),
    ;

    override fun toString() = description
}

private fun String.classpathReader(): () -> String = {
    Ipsum::class.java.getResource(this).readText()
}