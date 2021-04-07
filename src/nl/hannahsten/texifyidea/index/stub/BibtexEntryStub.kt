package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.NamedStub
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.BibtexEntry

interface BibtexEntryStub : StubElement<BibtexEntry?>, NamedStub<BibtexEntry?> {

    val title: String
    val authors: List<String?>
    val year: String
    val identifier: String
    override fun getName(): String? {
        return identifier
    }
}