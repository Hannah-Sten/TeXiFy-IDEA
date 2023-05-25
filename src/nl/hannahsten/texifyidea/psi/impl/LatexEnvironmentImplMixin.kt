package nl.hannahsten.texifyidea.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.IElementType
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub
import nl.hannahsten.texifyidea.psi.LatexEnvironment

abstract class LatexEnvironmentImplMixin : LatexEnvironment, StubBasedPsiElementBase<LatexEnvironmentStub> {


    constructor(stub: LatexEnvironmentStub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)
    constructor(node: ASTNode) : super(node)
    constructor(stub: LatexEnvironmentStub?, nodeType: IElementType?, node: ASTNode?) : super(stub, nodeType, node)


    override fun isValidHost(): Boolean {
        return true
    }

    override fun updateText(text: String): PsiLanguageInjectionHost {
        return ElementManipulators.handleContentChange(this, text)
    }

    override fun createLiteralTextEscaper(): LiteralTextEscaper<LatexEnvironment> {
        return LiteralTextEscaper.createSimple(this)
    }
}