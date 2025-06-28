package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.grammar.LatexLanguage
import nl.hannahsten.texifyidea.index.NewLabelsIndex
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.psi.getLabel
import nl.hannahsten.texifyidea.psi.impl.LatexEnvironmentImpl
import java.io.IOException

open class LatexEnvironmentStubElementType(debugName: String) : IStubElementType<LatexEnvironmentStub, LatexEnvironment>(debugName, LatexLanguage) {

    override fun createPsi(stub: LatexEnvironmentStub): LatexEnvironment {
        return LatexEnvironmentImpl(stub, this)
    }

    override fun createStub(psi: LatexEnvironment, parentStub: StubElement<*>): LatexEnvironmentStub {
        return LatexEnvironmentStubImpl(parentStub, this, psi.getEnvironmentName(), psi.getLabel() ?: "")
    }

    override fun getExternalId() = "texify.latex." + super.toString()

    @Throws(IOException::class)
    override fun serialize(stub: LatexEnvironmentStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.environmentName)
        dataStream.writeName(stub.label)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): LatexEnvironmentStub {
        val envName = dataStream.readName()?.string ?: ""
        val label = dataStream.readName()?.string // can be null if no label is present
        return LatexEnvironmentStubImpl(parentStub, this, envName, label)
    }

    override fun indexStub(stub: LatexEnvironmentStub, sink: IndexSink) {
        NewLabelsIndex.sinkIndexEnv(stub, sink)
    }
}