package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.*
import nl.hannahsten.texifyidea.LatexLanguage
import nl.hannahsten.texifyidea.index.LatexEnvironmentsIndex
import nl.hannahsten.texifyidea.index.LatexParameterLabeledEnvironmentsIndex
import nl.hannahsten.texifyidea.index.indexSinkOccurrence
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.impl.LatexEnvironmentImpl
import nl.hannahsten.texifyidea.util.magic.EnvironmentMagic
import java.io.IOException

open class LatexEnvironmentStubElementType(debugName: String) : IStubElementType<LatexEnvironmentStub, LatexEnvironment>(debugName, LatexLanguage) {

    override fun createPsi(stub: LatexEnvironmentStub): LatexEnvironment {
        return LatexEnvironmentImpl(stub, this)
    }

    override fun createStub(psi: LatexEnvironment, parentStub: StubElement<*>): LatexEnvironmentStub {
        return LatexEnvironmentStubImpl(parentStub, this, psi.environmentName, psi.label ?: "")
    }

    override fun getExternalId() = "ENVIRONMENT"

    @Throws(IOException::class)
    override fun serialize(stub: LatexEnvironmentStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.environmentName)
        dataStream.writeName(stub.label)
    }

    @Throws(IOException::class)
    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): LatexEnvironmentStub {
        val envName = dataStream.readName()?.string ?: ""
        val label = dataStream.readName()?.string ?: ""
        return LatexEnvironmentStubImpl(parentStub, this, envName, label)
    }

    override fun indexStub(stub: LatexEnvironmentStub, sink: IndexSink) {
        indexSinkOccurrence(sink, LatexEnvironmentsIndex, stub.environmentName)

        // only record environments with a label in the optional parameters
        if (stub.label.isNotEmpty() && EnvironmentMagic.labelAsParameter.contains(stub.environmentName)) {
            indexSinkOccurrence(sink, LatexParameterLabeledEnvironmentsIndex, stub.label)
        }
    }
}