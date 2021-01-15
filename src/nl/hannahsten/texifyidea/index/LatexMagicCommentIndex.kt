package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexMagicComment

class LatexMagicCommentIndex : StringStubIndexExtension<LatexMagicComment>() {

    companion object : IndexUtilBase<LatexMagicComment>(LatexMagicComment::class.java, IndexKeys.MAGIC_COMMENTS_KEY)

    @Suppress("RedundantCompanionReference")
    override fun getKey(): StubIndexKey<String, LatexMagicComment> = Companion.key()

    override fun getVersion() = LatexParserDefinition.FILE.stubVersion
}