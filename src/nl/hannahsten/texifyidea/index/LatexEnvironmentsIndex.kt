package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StringStubIndexExtension
import nl.hannahsten.texifyidea.psi.LatexEnvironment

class LatexEnvironmentsIndex : StringStubIndexExtension<LatexEnvironment>() {
    companion object : IndexUtilBase<LatexEnvironment>(LatexEnvironment::class.java, IndexKeys.ENVIRONMENTS_KEY)

    @Suppress("RedundantCompanionReference")
    override fun getKey() = Companion.key()
}