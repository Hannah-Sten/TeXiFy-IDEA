package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.index.LatexMagicCommentIndex
import nl.hannahsten.texifyidea.index.indexSinkOccurrence
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.psi.impl.LatexMagicCommentImpl
import nl.hannahsten.texifyidea.psi.key
import nl.hannahsten.texifyidea.psi.value
import java.io.IOException

open class LatexMagicCommentStubElementType(debugName: String) : IStubElementType<LatexMagicCommentStub, LatexMagicComment>(debugName, LatexLanguage) {

    override fun createPsi(stub: LatexMagicCommentStub): LatexMagicComment = LatexMagicCommentImpl(stub, this)

    override fun createStub(psi: LatexMagicComment, parentStub: StubElement<*>): LatexMagicCommentStub {
        return LatexMagicCommentStubImpl(parentStub, this, psi.key().toString(), psi.value())
    }

    override fun getExternalId(): String = "MAGIC_COMMENT"

    @Throws(IOException::class)
    override fun serialize(stub: LatexMagicCommentStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.key)
        dataStream.writeName(stub.value)
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): LatexMagicCommentStub {
        val key = dataStream.readName()?.string ?: ""
        val value = dataStream.readName()?.string ?: ""
        return LatexMagicCommentStubImpl(parentStub, this, key, value)
    }

    override fun indexStub(stub: LatexMagicCommentStub, sink: IndexSink) {
        indexSinkOccurrence(sink, LatexMagicCommentIndex, stub.key)
    }
}
