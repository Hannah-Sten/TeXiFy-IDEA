package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.index.LatexEnvironmentsIndex
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.impl.LatexEnvironmentImpl
import java.io.IOException

open class LatexEnvironmentStubElementType(private val debugName: String) : IStubElementType<LatexEnvironmentStub, LatexEnvironment>(debugName, LatexLanguage.INSTANCE) {
    override fun createPsi(stub: LatexEnvironmentStub): LatexEnvironment {
        return LatexEnvironmentImpl(stub, this)
    }

    override fun createStub(psi: LatexEnvironment, parentStub: StubElement<*>): LatexEnvironmentStub {
        return LatexEnvironmentStubImpl(parentStub, this, psi.environmentName)
    }

    override fun getExternalId() = debugName

    @Throws(IOException::class)
    override fun serialize(stub: LatexEnvironmentStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.environmentName)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): LatexEnvironmentStub {
        val envName = dataStream.readName()?.string ?: ""
        return LatexEnvironmentStubImpl(parentStub, this, envName)
    }

    override fun indexStub(stub: LatexEnvironmentStub, sink: IndexSink) {
        sink.occurrence(LatexEnvironmentsIndex.key(), stub.environmentName!!)
    }
}