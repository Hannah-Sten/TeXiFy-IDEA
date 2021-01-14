package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexEnvironment

/**
 * This index contains environments that define a label, i.e., environments which have
 * a label defined with an optional parameter.
 */
class LatexParameterLabeledEnvironmentsIndex : StringStubIndexExtension<LatexEnvironment>() {

    companion object : IndexUtilBase<LatexEnvironment>(LatexEnvironment::class.java, IndexKeys.LABELED_ENVIRONMENTS_KEY)

    @Suppress("RedundantCompanionReference")
    override fun getKey() = Companion.key()

    override fun getVersion() = LatexParserDefinition.FILE.stubVersion
}