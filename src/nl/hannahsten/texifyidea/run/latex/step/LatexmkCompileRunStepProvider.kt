package nl.hannahsten.texifyidea.run.latex.step

import nl.hannahsten.texifyidea.run.compiler.LatexCompiler

internal object LatexmkCompileRunStepProvider : LatexRunStepProvider {

    override val type: String = "latexmk-compile"

    override val aliases: Set<String> = setOf(
        type,
        "compile-latexmk",
        "latexmk",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = LatexCompileRunStep(
        id = type,
        compilerOverride = LatexCompiler.LATEXMK,
    )
}
