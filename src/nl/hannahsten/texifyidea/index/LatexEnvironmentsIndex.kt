package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexEnvironment

/**
 * This index contains all environments
 */
class LatexEnvironmentsIndex : StringStubIndexExtension<LatexEnvironment>() {

    object Util : IndexUtilBase<LatexEnvironment>(LatexEnvironment::class.java, LatexStubIndexKeys.ENVIRONMENTS_KEY)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}