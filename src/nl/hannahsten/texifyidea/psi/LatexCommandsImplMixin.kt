package nl.hannahsten.texifyidea.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub

/**
 * This class is a mixin for LatexCommandsImpl. We use a separate mixin class instead of [LatexPsiImplUtil] because we need to add an instance variable
 * in order to implement [getName] and [setName] correctly.
 */
abstract class LatexCommandsImplMixin : StubBasedPsiElementBase<LatexCommandsStub?>, PsiNameIdentifierOwner {

    @JvmField
    var name: String? = null

    constructor(stub: LatexCommandsStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
    constructor(stub: LatexCommandsStub?, nodeType: IElementType?, node: ASTNode?) : super(stub, nodeType, node)

    override fun toString(): String {
        return "LatexCommandsImpl(COMMANDS)[STUB]{" + getName() + "}"
    }

    override fun getTextOffset(): Int {
        val name = getName()
        return if (name == null) {
            super.getTextOffset()
        }
        else {
            val offset = node.text.indexOf(name)
            if (offset == -1) super.getTextOffset() else node.startOffset + offset
        }
    }

    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is LatexVisitor) {
            accept(visitor)
        }
        else {
            super.accept(visitor)
        }
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }
}
