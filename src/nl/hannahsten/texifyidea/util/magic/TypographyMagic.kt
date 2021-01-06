package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.util.mapOfVarargs

object TypographyMagic {

    /**
     * Matches each (supported) opening brace to it's opposite close brace.
     */
    @JvmField
    val braceOpposites = mapOfVarargs(
        "(", ")",
        "[", "]",
        "\\{", "\\}",
        "<", ">",
        "|", "|",
        "\\|", "\\|"
    )

    /**
     * Algorithmicx pairs (also hardcoded in lexer).
     */
    val pseudoCodeBeginEndOpposites = mapOf(
        "If" to "EndIf",
        "For" to "EndFor",
        "ForAll" to "EndFor",
        "While" to "EndWhile",
        "Repeat" to "Until",
        "Loop" to "EndLoop",
        "Function" to "EndFunction",
        "Procedure" to "EndProcedure"
    )
}