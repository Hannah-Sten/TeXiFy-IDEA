package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexEnvironment

data class LatexEnvironmentStubImpl(
    val parent: StubElement<*>,
    val elementType: IStubElementType<LatexEnvironmentStub, LatexEnvironment>,
    override val environmentName: String,
    override val label: String?
) :
    StubBase<LatexEnvironment>(parent, elementType), LatexEnvironmentStub