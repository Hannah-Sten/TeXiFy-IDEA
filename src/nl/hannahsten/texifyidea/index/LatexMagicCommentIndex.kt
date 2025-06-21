package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexMagicComment

class LatexMagicCommentIndex : StringStubIndexExtension<LatexMagicComment>() {

    object Util : IndexUtilBase<LatexMagicComment>(LatexMagicComment::class.java, LatexStubIndexKeys.MAGIC_COMMENTS_KEY)

    override fun getKey(): StubIndexKey<String, LatexMagicComment> = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}