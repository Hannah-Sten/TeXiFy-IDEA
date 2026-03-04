package nl.hannahsten.texifyidea.run.latex.steplog

import com.intellij.openapi.vfs.VirtualFile

internal data class ParsedStepMessage(
    val message: String,
    val level: ParsedStepMessageLevel,
    val fileName: String? = null,
    val line: Int? = null,
    val file: VirtualFile? = null,
)

internal enum class ParsedStepMessageLevel {

    ERROR,
    WARNING,
}
