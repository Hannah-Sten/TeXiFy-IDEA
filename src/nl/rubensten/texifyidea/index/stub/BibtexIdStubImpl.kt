package nl.rubensten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import nl.rubensten.texifyidea.psi.BibtexId

/**
 * @author Ruben Schellekens
 */
data class BibtexIdStubImpl(
        val parent: StubElement<*>?,
        val elementType: IStubElementType<BibtexIdStub, BibtexId>,
        override val identifier: String
) : NamedStubBase<BibtexId>(parent, elementType, identifier), BibtexIdStub