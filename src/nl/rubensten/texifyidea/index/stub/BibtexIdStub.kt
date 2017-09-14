package nl.rubensten.texifyidea.index.stub

import com.intellij.psi.stubs.NamedStub
import com.intellij.psi.stubs.StubElement
import nl.rubensten.texifyidea.psi.BibtexId

/**
 * @author Ruben Schellekens
 */
interface BibtexIdStub : StubElement<BibtexId>, NamedStub<BibtexId> {

    val identifier: String
}