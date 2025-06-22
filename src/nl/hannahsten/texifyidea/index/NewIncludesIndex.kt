package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.LatexCommands

open class NewIncludesIndexEx : NewLatexCommandsStubIndex() {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.INCLUDES
    }
}

object NewIncludesIndex : NewIncludesIndexEx()