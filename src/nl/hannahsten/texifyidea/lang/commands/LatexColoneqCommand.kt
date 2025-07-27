package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.MATHTOOLS
import kotlin.io.path.Path

/**
 * @author Hannah Schellekens
 */
enum class LatexColoneqCommand(
    override val command: String,
    override vararg val arguments: Argument = emptyArray(),
    override val dependency: LatexPackage = LatexPackage.DEFAULT,
    override val display: String? = null,
    override val isMathMode: Boolean = true,
    val collapse: Boolean = false
) : LatexCommand {

    COLON_EQUALSS("coloneqq", dependency = MATHTOOLS, display = ":=", collapse = true),
    EQUALSS_COLON("eqqcolon", dependency = MATHTOOLS, display = "=:", collapse = true),
    COLON_EQUALS("coloneq", dependency = MATHTOOLS, display = ":–", collapse = true),
    EQUALS_COLON("eqcolon", dependency = MATHTOOLS, display = "–:", collapse = true),
    COLON_APPROX("colonapprox", dependency = MATHTOOLS, display = ":≈", collapse = true),
    COLON_SIM("colonsim", dependency = MATHTOOLS, display = ":∼", collapse = true),
    DOUBLE_COLON("dblcolon", dependency = MATHTOOLS, display = "::", collapse = true),
    DOUBLE_COLON_EQUALSS("Coloneqq", dependency = MATHTOOLS, display = "::=", collapse = true),
    EQUALSS_DOUBLE_COLON("Eqqcolon", dependency = MATHTOOLS, display = "=::", collapse = true),
    DOUBLE_COLON_EQUALS("Coloneq", dependency = MATHTOOLS, display = "::–", collapse = true),
    EQUALS_DOUBLE_COLON("Eqcolon", dependency = MATHTOOLS, display = "–::", collapse = true),
    DOUBLE_COLON_APPROX("Colonapprox", dependency = MATHTOOLS, display = "::≈", collapse = true),
    DOUBLE_COLON_SIM("Colonsim", dependency = MATHTOOLS, display = "::∼", collapse = true),
    ;

    override val identifier: String
        get() = name
}
