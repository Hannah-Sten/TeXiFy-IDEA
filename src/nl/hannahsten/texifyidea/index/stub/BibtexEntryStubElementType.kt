package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.grammar.BibtexLanguage
import nl.hannahsten.texifyidea.index.BibtexEntryIndex
import nl.hannahsten.texifyidea.psi.BibtexEntry
import nl.hannahsten.texifyidea.psi.impl.BibtexEntryImpl

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
        return BibtexEntryStubImpl(parentStub, this, entry.identifier, entry.authors, entry.year, entry.title)
    }

    override fun getExternalId() = "texify.bibtex." + super.toString()

    override fun indexStub(stub: BibtexEntryStub, sink: IndexSink) {
        sink.occurrence(BibtexEntryIndex.key, stub.name ?: "")
    }
}