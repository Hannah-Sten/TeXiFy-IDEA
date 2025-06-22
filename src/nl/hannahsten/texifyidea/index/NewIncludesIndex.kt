package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.LatexCommands

object NewIncludesIndex : NewLatexCommandsStubIndex() {

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.INCLUDES
    }
}