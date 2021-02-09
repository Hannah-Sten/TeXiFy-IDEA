package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.run.bibtex.compiler.BiberCompiler
import nl.hannahsten.texifyidea.run.bibtex.compiler.BibtexCompiler
import nl.hannahsten.texifyidea.run.bibtex.compiler.SupportedBibliographyCompiler
import nl.hannahsten.texifyidea.run.latex.compiler.*
import nl.hannahsten.texifyidea.run.step.BibliographyCompileStepProvider
import nl.hannahsten.texifyidea.run.step.CompileLatexCompileStepProvider
import nl.hannahsten.texifyidea.run.step.LatexCompileStepProvider

object CompilerMagic {

    val compileStepProviders: LinkedHashMap<String, LatexCompileStepProvider> = linkedMapOf(
        CompileLatexCompileStepProvider.id to CompileLatexCompileStepProvider,
        BibliographyCompileStepProvider.id to BibliographyCompileStepProvider,
    )

    val latexCompilerByExecutableName: Map<String, SupportedLatexCompiler> = mapOf(
        "pdflatex" to PdflatexCompiler,
        "lualatex" to LualatexCompiler,
        "latexmk" to LatexmkCompiler,
        "xelatex" to XelatexCompiler,
        "texliveonfly" to TexliveonflyCompiler,
        "tectonic" to TectonicCompiler,
    )

    val bibliographyCompilerByExecutableName: Map<String, SupportedBibliographyCompiler> = mapOf(
        "bibtex" to BibtexCompiler,
        "biber" to BiberCompiler,
    )
}