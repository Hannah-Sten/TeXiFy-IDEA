package nl.hannahsten.texifyidea.util.magic

object TypographyMagic {

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