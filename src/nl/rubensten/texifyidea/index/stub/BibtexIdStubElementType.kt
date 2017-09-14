package nl.rubensten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.rubensten.texifyidea.BibtexLanguage
import nl.rubensten.texifyidea.index.BibtexIdIndex
import nl.rubensten.texifyidea.psi.BibtexId
import nl.rubensten.texifyidea.psi.impl.BibtexIdImpl

/**
 * @author Ruben Schellekens
 */
open class BibtexIdStubElementType(val debugName: String) : IStubElementType<BibtexIdStub, BibtexId>("bibtex-id", BibtexLanguage) {

    override fun createPsi(stub: BibtexIdStub): BibtexId {
        val id = BibtexIdImpl(stub, this)
        val name = stub.name ?: return id
        id.setName(name)
        return id
    }

    override fun createStub(entry: BibtexId, parent: StubElement<*>?): BibtexIdStub {
        val identifier = entry.text
        entry.setName(identifier)

        return BibtexIdStubImpl(parent, this, identifier)
    }

    override fun getExternalId() = "texify.bibtex.id"

    override fun serialize(stub: BibtexIdStub, out: StubOutputStream) {
        out.writeName(stub.name)
    }

    override fun deserialize(input: StubInputStream, parent: StubElement<*>?): BibtexIdStub {
        val name = input.readName().toString()
        return BibtexIdStubImpl(parent, this, name)
    }

    override fun indexStub(stub: BibtexIdStub, indexSink: IndexSink) {
        indexSink.occurrence(BibtexIdIndex.KEY, stub.name ?: "")
    }
}