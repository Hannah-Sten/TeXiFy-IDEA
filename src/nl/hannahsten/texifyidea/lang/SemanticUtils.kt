package nl.hannahsten.texifyidea.lang

fun LSemanticCommand.introduces(candidate: LatexContext): Boolean {
    return arguments.any {
        it.contextSignature.introduces(candidate)
    }
}

fun LSemanticCommand.introducesAny(candidates: LContextSet): Boolean {
    return arguments.any {
        it.contextSignature.introducesAny(candidates)
    }
}