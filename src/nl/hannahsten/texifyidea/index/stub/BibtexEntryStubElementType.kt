package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.index.BibtexEntryIndexKey
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.impl.BibtexEntryImpl
import nl.hannahsten.texifyidea.util.parser.getAuthors
import nl.hannahsten.texifyidea.util.parser.getIdentifier
import nl.hannahsten.texifyidea.util.parser.getTitle
import nl.hannahsten.texifyidea.util.parser.getYear

open class BibtexEntryStubElementType(debugName: String) : IStubElementType<BibtexEntryStub, BibtexEntry>(debugName, BibtexLanguage) {

    override fun createPsi(stub: BibtexEntryStub): BibtexEntry {
        return BibtexEntryImpl(stub, this)
    }

    override fun serialize(stub: BibtexEntryStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.identifier)
        dataStream.writeName(stub.title)
        dataStream.writeName(stub.authors.joinToString(";"))
        dataStream.writeName(stub.year)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): BibtexEntryStub {
        val name = dataStream.readName()?.string ?: ""
        val title = dataStream.readName()?.string ?: ""
        val authors = (dataStream.readName()?.string ?: "").split(";")
        val year = dataStream.readName()?.string ?: ""
        return BibtexEntryStubImpl(parentStub, this, name, authors, year, title)
    }

    override fun createStub(entry: BibtexEntry, parentStub: StubElement<*>?): BibtexEntryStub {
        return BibtexEntryStubImpl(parentStub, this, entry.getIdentifier(), entry.getAuthors(), entry.getYear(), entry.getTitle())
    }

    override fun getExternalId() = "texify.bibtex." + super.toString()

    override fun indexStub(stub: BibtexEntryStub, sink: IndexSink) {
        sink.occurrence(BibtexEntryIndexKey.KEY, stub.name ?: "")
    }
}