package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.BibtexEntry

data class BibtexEntryStubImpl(
    val parent: StubElement<*>?,
    val elementType: IStubElementType<BibtexEntryStub, BibtexEntry>,
    val myIdentifier: String,
    val myAuthors: List<String>,
    val myYear: String,
    val myTitle: String
) : NamedStubBase<BibtexEntry>(parent, elementType, myIdentifier), BibtexEntryStub {

    override val identifier = myIdentifier

    override val authors = myAuthors

    override val title = myTitle

    override val year = myYear

    override fun getName() = myIdentifier

    override fun toString(): String = "BibtexEntryStubImpl(myIdentifier='$myIdentifier', myAuthors=$myAuthors, myYear='$myYear', myTitle='$myTitle')"
}