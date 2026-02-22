package nl.hannahsten.texifyidea.run.latex.step

internal object LatexCompileRunStepProvider : LatexRunStepProvider {

    override val type: String = "latex-compile"

    override val aliases: Set<String> = setOf(
        type,
        "compile-latex",
    )

    override fun create(spec: LatexRunStepSpec): LatexRunStep = LatexCompileRunStep()
}
