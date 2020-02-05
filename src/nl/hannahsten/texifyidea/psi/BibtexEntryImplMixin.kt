package nl.hannahsten.texifyidea.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import nl.hannahsten.texifyidea.index.stub.BibtexEntryStub

open class BibtexEntryImplMixin : StubBasedPsiElementBase<BibtexEntryStub?>, PsiNameIdentifierOwner {
    constructor(node: ASTNode) : super(node)
    constructor(stub: BibtexEntryStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(stub: BibtexEntryStub?, nodeType: IElementType?, node: ASTNode?) : super(stub, nodeType, node)

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    @Throws(IncorrectOperationException::class)
    override fun setName(name: String): PsiElement {
        // TODO should this do something?
        return this
    }
}