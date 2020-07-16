package nl.hannahsten.texifyidea.run.latex.logtab

import com.intellij.util.ui.MessageCategory

data class LatexLogMessage(val message: String, val fileName: String? = null, val line: Int = -1, val type: LatexLogMessageType = LatexLogMessageType.ERROR)

enum class LatexLogMessageType(val category: Int) {
    ERROR(MessageCategory.ERROR),
    WARNING(MessageCategory.WARNING),
}