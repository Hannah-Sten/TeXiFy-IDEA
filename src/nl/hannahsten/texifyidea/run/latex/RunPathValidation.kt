package nl.hannahsten.texifyidea.run.latex

internal fun isInvalidJetBrainsBinPath(path: String?): Boolean = path?.endsWith("/bin") == true
