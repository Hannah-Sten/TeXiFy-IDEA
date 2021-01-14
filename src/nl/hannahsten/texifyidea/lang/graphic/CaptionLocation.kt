package nl.hannahsten.texifyidea.lang.graphic

import nl.hannahsten.texifyidea.lang.Described

/**
 * Where to put the caption inside a figure environment.
 *
 * @author Hannah Schellekens
 */
enum class CaptionLocation(override val description: String) : Described {

    ABOVE_GRAPHIC("above graphic"),
    BELOW_GRAPHIC("below graphic");

    override fun toString() = description
}