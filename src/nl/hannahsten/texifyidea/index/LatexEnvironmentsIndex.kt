package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.LatexParserDefinition
import nl.hannahsten.texifyidea.psi.LatexEnvironment

/**
 * This index contains all environments
 */
class LatexEnvironmentsIndex : StringStubIndexExtension<LatexEnvironment>() {

    companion object : IndexUtilBase<LatexEnvironment>(LatexEnvironment::class.java, IndexKeys.ENVIRONMENTS_KEY)

    @Suppress("RedundantCompanionReference")
    override fun getKey() = Companion.key()

    override fun getVersion() = LatexParserDefinition.FILE.stubVersion
}