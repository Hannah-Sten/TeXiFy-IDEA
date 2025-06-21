package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.grammar.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexEnvironment

/**
 * This index contains environments that define a label, i.e., environments which have
 * a label defined with an optional parameter.
 */
class LatexParameterLabeledEnvironmentsIndex : StringStubIndexExtension<LatexEnvironment>() {

    object Util : IndexUtilBase<LatexEnvironment>(LatexEnvironment::class.java, LatexStubIndexKeys.LABELED_ENVIRONMENTS_KEY)

    override fun getKey() = Util.key()

    override fun getVersion() = LatexParserDefinition.Cache.FILE.stubVersion
}