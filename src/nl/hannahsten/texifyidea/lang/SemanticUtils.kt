package nl.hannahsten.texifyidea.lang

fun LSemanticCommand.introduces(candidate: LatexContext): Boolean = arguments.any {
    it.contextSignature.introduces(candidate)
}

fun LSemanticCommand.introducesAny(candidates: LContextSet): Boolean = arguments.any {
    it.contextSignature.introducesAny(candidates)
}

fun LSemanticEnv.introduces(candidate: LatexContext): Boolean = contextSignature.introduces(candidate) ||
    arguments.any {
        it.contextSignature.introduces(candidate)
    }

fun LSemanticEntity.introduces(candidate: LatexContext): Boolean = when (this) {
    is LSemanticCommand -> introduces(candidate)
    is LSemanticEnv -> introduces(candidate)
}