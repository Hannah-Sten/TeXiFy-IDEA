package nl.hannahsten.texifyidea.run.latex.logtab

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.MessageCategory

data class LatexLogMessage(val message: String, val fileName: String? = null, val line: Int = -1, val type: LatexLogMessageType = LatexLogMessageType.ERROR, val file: VirtualFile? = null)

enum class LatexLogMessageType(val category: Int) {
    ERROR(MessageCategory.ERROR),
    WARNING(MessageCategory.WARNING),
}