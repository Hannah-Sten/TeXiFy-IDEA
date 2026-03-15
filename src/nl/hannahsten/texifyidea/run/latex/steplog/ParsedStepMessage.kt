package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile

internal data class ParsedStepMessage(
    val message: String,
    val level: ParsedStepMessageLevel,
    val fileName: String? = null,
    val line: Int? = null,
    val file: VirtualFile? = null,
    val source: ParsedStepMessageSource = ParsedStepMessageSource.LATEX,
)

internal sealed interface ParsedStepEvent {

    data class Message(
        val message: ParsedStepMessage,
    ) : ParsedStepEvent

    object ResetLatexMessages : ParsedStepEvent
}

internal enum class ParsedStepMessageLevel {

    ERROR,
    WARNING,
}

internal enum class ParsedStepMessageSource {

    LATEX,
    BIBTEX,
}
