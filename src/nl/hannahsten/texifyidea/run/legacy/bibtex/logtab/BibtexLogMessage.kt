package nl.hannahsten.texifyidea.run.legacy.bibtex.logtab

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.MessageCategory
import nl.hannahsten.texifyidea.util.capitalizeFirst
import java.util.*

data class BibtexLogMessage(val message: String, val fileName: String? = null, val line: Int? = -1, val type: BibtexLogMessageType, val file: VirtualFile? = null) {

    fun toTreeViewString(): String {
        val typeString = type.toString().toLowerCase().capitalizeFirst()
        val lineString = if (line != null && line >= 0) "line ($line)" else ""
        return "$typeString:$lineString $message"
    }
}

enum class BibtexLogMessageType(val category: Int) {

    ERROR(MessageCategory.ERROR),
    WARNING(MessageCategory.WARNING);

    override fun toString(): String {
        return super.toString().toLowerCase()
    }
}