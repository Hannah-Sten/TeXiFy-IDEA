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

fun LSemanticEnv.introduces(candidate: LatexContext): Boolean {
    return contextSignature.introduces(candidate) ||
        arguments.any {
            it.contextSignature.introduces(candidate)
        }
}

fun LSemanticEntity.introduces(candidate: LatexContext): Boolean {
    return when (this) {
        is LSemanticCommand -> introduces(candidate)
        is LSemanticEnv -> introduces(candidate)
    }
}