package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.psi.LatexEnvironment

class LatexLabeledEnvironmentsIndex : StringStubIndexExtension<LatexEnvironment>() {
    companion object : IndexUtilBase<LatexEnvironment>(LatexEnvironment::class.java, IndexKeys.LABELED_ENVIRONMENTS_KEY)

    @Suppress("RedundantCompanionReference")
    override fun getKey() = Companion.key()
}