package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.run.compiler.*
import nl.hannahsten.texifyidea.run.latex.step.BibliographyCompileStepProvider
import nl.hannahsten.texifyidea.run.latex.step.CompileLatexCompileStepProvider

object CompilerMagic {

    val compilerByExecutableName = mapOf(
        "pdflatex" to PdflatexCompiler.INSTANCE,
        "lualatex" to LualatexCompiler.INSTANCE,
        "latexmk" to LatexmkCompiler.INSTANCE,
        "xelatex" to XelatexCompiler.INSTANCE,
        "texliveonfly" to TexliveonflyCompiler.INSTANCE,
        "tectonic" to TectonicCompiler.INSTANCE,
    )

    val compileStepProviders = listOf(
        CompileLatexCompileStepProvider,
        BibliographyCompileStepProvider,
    )
}