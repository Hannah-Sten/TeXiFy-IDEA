package nl.hannahsten.texifyidea.lang.commands

import nl.hannahsten.texifyidea.lang.LatexPackage
import nl.hannahsten.texifyidea.lang.LatexPackage.Companion.ALGPSEUDOCODE

/**
 * @author Hannah Schellekens
 */
enum class LatexAlgorithmicxCommand(
        override val command: String,
        override vararg val arguments: Argument = emptyArray(),
        override val dependency: LatexPackage = LatexPackage.DEFAULT,
        override val display: String? = null,
        override val isMathMode: Boolean = false,
        val collapse: Boolean = false
) : LatexCommand {

    FOR("For", "condition".asRequired(), dependency = ALGPSEUDOCODE),
    FORALL("ForAll", "condition".asRequired(), dependency = ALGPSEUDOCODE),
    ENDFOR("EndFor", dependency = ALGPSEUDOCODE),
    IF_ALGPSEUDOCODE("If", "condition".asRequired(), dependency = ALGPSEUDOCODE),
    ELSIF("ElsIf", "condition".asRequired(), dependency = ALGPSEUDOCODE),
    ENDIF("EndIf", dependency = ALGPSEUDOCODE),
    WHILE("While", "condition".asRequired(), dependency = ALGPSEUDOCODE),
    ENDWHILE("EndWhile", dependency = ALGPSEUDOCODE),
    REPEAT("Repeat", dependency = ALGPSEUDOCODE),
    UNTIL("Until", "condition".asRequired(), dependency = ALGPSEUDOCODE),
    LOOP("Loop", dependency = ALGPSEUDOCODE),
    ENDLOOP("EndLoop", dependency = ALGPSEUDOCODE),
    FUNCTION("Function", "name".asRequired(), "params".asRequired(), dependency = ALGPSEUDOCODE),
    ENDFUNCTION("EndFunction", dependency = ALGPSEUDOCODE),
    PROCEDURE("Procedure", "name".asRequired(), "params".asRequired(), dependency = ALGPSEUDOCODE),
    ENDPROCEDURE("EndProcedure", dependency = ALGPSEUDOCODE),
    ;

    override val identifyer: String
        get() = name
}