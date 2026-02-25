package nl.hannahsten.texifyidea.run.latex

internal object StepUiOptionIds {

    const val LATEX_COMPILE = "latex-compile"
    const val LATEXMK_COMPILE = "latexmk-compile"
    const val PDF_VIEWER = "pdf-viewer"
    // Unused: const val FILE_CLEANUP = "file-cleanup"

    const val COMPILE_PATH = "compile.path"
    const val COMPILE_ARGS = "compile.args"
    const val COMPILE_OUTPUT_FORMAT = "compile.outputFormat"
    const val LATEXMK_MODE = "latexmk.mode"
    const val LATEXMK_CUSTOM_ENGINE = "latexmk.customEngine"
    const val LATEXMK_CITATION = "latexmk.citation"
    const val LATEXMK_EXTRA_ARGS = "latexmk.extraArgs"
    const val VIEWER_REQUIRE_FOCUS = "viewer.requireFocus"
    const val VIEWER_COMMAND = "viewer.command"
    const val STEP_WORKING_DIRECTORY = "step.workingDirectory"

    const val BIB_COMPILER_PATH = "bib.compilerPath"
    const val BIB_COMPILER_ARGS = "bib.compilerArgs"

    const val MAKEINDEX_ARGS = "makeindex.args"
    const val MAKEINDEX_TARGET_BASE = "makeindex.targetBase"

    const val COMMAND_EXECUTABLE = "command.executable"
    const val COMMAND_ARGS = "command.args"
}
