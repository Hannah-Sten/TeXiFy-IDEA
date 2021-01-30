package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.run.compiler.*

object CompilerMagic {

    val compilerByExecutableName = mapOf(
        "pdflatex" to PdflatexCompiler.INSTANCE,
        "lualatex" to LualatexCompiler.INSTANCE,
        "latexmk" to LatexmkCompiler.INSTANCE,
        "xelatex" to XelatexCompiler.INSTANCE,
        "texliveonfly" to TexliveonflyCompiler.INSTANCE,
        "tectonic" to TectonicCompiler.INSTANCE,
    )
}