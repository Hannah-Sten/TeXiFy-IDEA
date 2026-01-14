package nl.hannahsten.texifyidea.index

import nl.hannahsten.texifyidea.psi.LatexMagicComment

class NewMagicCommentIndexEx : LatexCompositeStubIndex<LatexMagicComment>(LatexMagicComment::class.java) {
    override fun getVersion(): Int = 3
    override fun getKey() = LatexStubIndexKeys.MAGIC_COMMENTS_KEY
}

val NewMagicCommentIndex = NewMagicCommentIndexEx()