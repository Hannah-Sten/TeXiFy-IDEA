package nl.hannahsten.texifyidea.index.stub

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.BibtexLanguage
import nl.hannahsten.texifyidea.index.BibtexIdIndex
import nl.hannahsten.texifyidea.psi.BibtexId
import nl.hannahsten.texifyidea.psi.impl.BibtexIdImpl
import nl.hannahsten.texifyidea.util.substringEnd

/**
 * @author Hannah Schellekens
 */
open class BibtexIdStubElementType(val debugName: String) : IStubElementType<BibtexIdStub, BibtexId>("bibtex-id", BibtexLanguage) {

    override fun createPsi(stub: BibtexIdStub): BibtexId {
        val id = BibtexIdImpl(stub, this)
        val name = stub.name ?: return id
        id.setName(name)
        return id
    }

    override fun createStub(entry: BibtexId, parent: StubElement<*>?): BibtexIdStub {
        val identifier = entry.text.substringEnd(1)
        entry.setName(identifier)

        return BibtexIdStubImpl(parent, this, identifier)
    }

    override fun getExternalId() = "texify.bibtex.id"

    override fun serialize(stub: BibtexIdStub, out: StubOutputStream) {
        out.writeName(stub.name)
    }

    override fun deserialize(input: StubInputStream, parent: StubElement<*>?): BibtexIdStub {
        val name = input.readName()?.string ?: ""
        return BibtexIdStubImpl(parent, this, name)
    }

    override fun indexStub(stub: BibtexIdStub, indexSink: IndexSink) {
        indexSink.occurrence(BibtexIdIndex.key, stub.name ?: "")
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
        val psi = node?.psi as? BibtexId ?: return false
        return !psi.text.substringEnd(1).isEmpty()
    }
}