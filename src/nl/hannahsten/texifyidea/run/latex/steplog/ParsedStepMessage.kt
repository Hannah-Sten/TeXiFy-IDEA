package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile

internal data class ParsedStepMessage(
    val message: String,
    val level: ParsedStepMessageLevel,
    val fileName: String? = null,
    val line: Int? = null,
    val file: VirtualFile? = null,
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
