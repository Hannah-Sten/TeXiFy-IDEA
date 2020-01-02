package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.NamedStubBase
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.BibtexEntry

data class BibtexEntryStubImpl(val parent: StubElement<*>?,
                               val elementType: IStubElementType<BibtexEntryStub, BibtexEntry>,
                               val myIdentifier: String,
                               val myAuthors: List<String>,
                               val myYear: String,
                               val myTitle: String) : NamedStubBase<BibtexEntry>(parent, elementType, myIdentifier), BibtexEntryStub {

    override fun getIdentifier() = myIdentifier

    override fun getAuthors() = myAuthors

    override fun getTitle() = myTitle

    override fun getYear() = myYear

    override fun getName() = myIdentifier

    override fun toString(): String {
        return "BibtexEntryStubImpl(myIdentifier='$myIdentifier', myAuthors=$myAuthors, myYear='$myYear', myTitle='$myTitle')"
    }
}