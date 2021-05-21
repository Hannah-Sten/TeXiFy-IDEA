package nl.hannahsten.texifyidea.util.magic

import nl.hannahsten.texifyidea.run.compiler.latex.*
import nl.hannahsten.texifyidea.run.compiler.bibtex.BiberCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.BibtexCompiler
import nl.hannahsten.texifyidea.run.compiler.bibtex.SupportedBibliographyCompiler
import nl.hannahsten.texifyidea.run.step.BibliographyCompileStepProvider
import nl.hannahsten.texifyidea.run.step.LatexCompileStepProvider
import nl.hannahsten.texifyidea.run.step.StepProvider

object CompilerMagic {

    val compileStepProviders: LinkedHashMap<String, StepProvider> = linkedMapOf(
        LatexCompileStepProvider.id to LatexCompileStepProvider,
        BibliographyCompileStepProvider.id to BibliographyCompileStepProvider,
    )

    val latexCompilerByExecutableName: Map<String, SupportedLatexCompiler> = mapOf(
        "pdflatex" to PdflatexCompiler,
        "lualatex" to LualatexCompiler,
        "latexmk" to LatexmkCompiler,
        "xelatex" to XelatexCompiler,
        "texliveonfly" to TexliveonflyCompiler,
        "tectonic" to TectonicCompiler,
        "arara" to AraraCompiler
    )

    val bibliographyCompilerByExecutableName: Map<String, SupportedBibliographyCompiler> = mapOf(
        "bibtex" to BibtexCompiler,
        "biber" to BiberCompiler,
    )
}