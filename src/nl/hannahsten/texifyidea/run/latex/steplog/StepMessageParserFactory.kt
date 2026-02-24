package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile

internal object StepMessageParserFactory {

    fun create(stepType: String, mainFile: VirtualFile?): StepMessageParserSession = when (stepType) {
        "latex-compile",
        "latexmk-compile" -> LatexStepMessageParserSession(mainFile)

        "legacy-bibtex" -> BibtexStepMessageParserSession(mainFile)

        else -> NoopStepMessageParser
    }
}
