package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.BibtexId

/**
 * @author Hannah Schellekens
 */
data class BibtexIdStubImpl(
        val parent: StubElement<*>?,
        val elementType: IStubElementType<BibtexIdStub, BibtexId>,
        val myIdentifier: String
) : NamedStubBase<BibtexId>(parent, elementType, myIdentifier), BibtexIdStub {

    override fun getIdentifier() = myIdentifier

    override fun getName() = identifier

    override fun toString(): String {
        return "BibtexIdStubImpl(parent=$parent, elementType=$elementType, identifier='$identifier')"
    }
}