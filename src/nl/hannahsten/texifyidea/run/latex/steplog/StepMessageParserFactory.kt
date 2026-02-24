package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile
import nl.hannahsten.texifyidea.run.latex.LatexStepType

internal object StepMessageParserFactory {

    fun create(stepType: String, mainFile: VirtualFile?): StepMessageParserSession = when (stepType) {
        LatexStepType.LATEX_COMPILE,
        LatexStepType.LATEXMK_COMPILE -> LatexStepMessageParserSession(mainFile)

        LatexStepType.BIBTEX -> BibtexStepMessageParserSession(mainFile)

        else -> NoopStepMessageParser
    }
}
