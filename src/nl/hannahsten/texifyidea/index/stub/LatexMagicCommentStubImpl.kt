package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexMagicComment

class LatexMagicCommentStubImpl(
    val parent: StubElement<*>,
    val elementType: IStubElementType<LatexMagicCommentStub, LatexMagicComment>,
    override val key: String,
    override val value: String?
) : StubBase<LatexMagicComment>(parent, elementType), LatexMagicCommentStub