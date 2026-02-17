package nl.hannahsten.texifyidea.run.latex

data class LatexCompilationCapabilities(
    val handlesBib: Boolean,
    val handlesMakeindex: Boolean,
    val handlesCompileCount: Boolean,
    val supportsAuxDir: Boolean,
    val supportsOutputFormatSet: Boolean,
)
