package nl.hannahsten.texifyidea.index.stub

import com.intellij.psi.stubs.StubElement
import nl.hannahsten.texifyidea.psi.LatexMagicComment

interface LatexMagicCommentStub : StubElement<LatexMagicComment?> {

    val key: String
    val value: String?
}