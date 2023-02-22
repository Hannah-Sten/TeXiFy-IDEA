package nl.hannahsten.texifyidea.index.file

import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.IOUtil
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.remotelibraries.state.BibtexEntryListConverter
import java.io.BufferedInputStream
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutput
import java.nio.charset.Charset

object BibtexEntryExternalizer : DataExternalizer<BibtexEntry> {
    override fun save(out: DataOutput, value: BibtexEntry) {
        val bibString = BibtexEntryListConverter.toString(listOf(value))
        IOUtil.writeUTF(out, bibString)
    }

    override fun read(`in`: DataInput): BibtexEntry? {
        return if (`in` is DataInputStream) {
            val bibString = BufferedInputStream(`in`).reader(Charset.defaultCharset()).readLines().joinToString("\n")
            BibtexEntryListConverter.fromString(bibString).firstOrNull()
        }
        else null
    }
}