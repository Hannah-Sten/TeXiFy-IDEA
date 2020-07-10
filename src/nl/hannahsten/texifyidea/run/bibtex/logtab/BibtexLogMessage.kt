package nl.hannahsten.texifyidea.run.bibtex.logtab

import com.intellij.util.ui.MessageCategory

data class BibtexLogMessage(val message: String, val fileName: String? = null, val line: Int? = 0, val type: BibtexLogMessageType)

enum class BibtexLogMessageType(val category: Int) {
    ERROR(MessageCategory.ERROR),
    WARNING(MessageCategory.WARNING),
}