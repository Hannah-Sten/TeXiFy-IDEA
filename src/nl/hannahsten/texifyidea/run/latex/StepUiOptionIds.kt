package nl.hannahsten.texifyidea.run.latex

internal object StepUiOptionIds {

    const val LATEX_COMPILE = "latex-compile"
    const val LATEXMK_COMPILE = "latexmk-compile"
    const val PDF_VIEWER = "pdf-viewer"

    const val COMPILE_PATH = "compile.path"
    const val COMPILE_ARGS = "compile.args"
    const val COMPILE_OUTPUT_FORMAT = "compile.outputFormat"
    const val COMPILE_DISTRIBUTION = "compile.distribution"
    const val LATEXMK_MODE = "latexmk.mode"
    const val LATEXMK_CUSTOM_ENGINE = "latexmk.customEngine"
    const val LATEXMK_CITATION = "latexmk.citation"
    const val LATEXMK_EXTRA_ARGS = "latexmk.extraArgs"
    const val VIEWER_REQUIRE_FOCUS = "viewer.requireFocus"
    const val VIEWER_COMMAND = "viewer.command"

    val supportedOptionIdsByType: Map<String, Set<String>> = mapOf(
        LATEX_COMPILE to setOf(
            COMPILE_PATH,
            COMPILE_ARGS,
            COMPILE_OUTPUT_FORMAT,
            COMPILE_DISTRIBUTION,
        ),
        LATEXMK_COMPILE to setOf(
            COMPILE_PATH,
            COMPILE_ARGS,
            COMPILE_DISTRIBUTION,
            LATEXMK_MODE,
            LATEXMK_CUSTOM_ENGINE,
            LATEXMK_CITATION,
            LATEXMK_EXTRA_ARGS,
        ),
        PDF_VIEWER to setOf(
            VIEWER_REQUIRE_FOCUS,
            VIEWER_COMMAND,
        ),
    )
}
