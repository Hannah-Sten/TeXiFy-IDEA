package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.psi.LatexCommands

object NewCommandsIndex : NewLatexCommandsIndex() {
    override fun getKey(): StubIndexKey<String?, LatexCommands?> {
        return LatexStubIndexKeys.COMMANDS
    }
}