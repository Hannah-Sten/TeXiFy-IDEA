package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class NewCommandsIndexEx : NewLatexCommandsStubIndex() {

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS
    }
}

val NewCommandsIndex = NewCommandsIndexEx()

/**
 * Definitions of both commands and theorems
 */
class NewDefinitionIndexEx : NewLatexCommandsTransformedStubIndex() {
    override fun getVersion(): Int {
        return 1002
    }

    private fun getDefinitionName(stub: LatexCommandsStub): String? {
        if (stub.requiredParams.isNotEmpty()) {
            return stub.requiredParams[0]
        }
        val children = stub.parentStub!!.childrenStubs
        val siblingIndex = children.indexOfFirst { it === stub }
        if (siblingIndex == -1) return null
        val sibling = children[siblingIndex] as? LatexCommandsStub ?: return null
        return sibling.commandToken
    }

    override fun sinkIndex(stub: LatexCommandsStub, sink: IndexSink) {
        val command = stub.commandToken
        if (command !in CommandMagic.definitions) return
        getDefinitionName(stub)?.let {
            sink.occurrence(key, it)
        }
    }

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.DEFINITIONS_NEW
    }
}

val NewDefinitionIndex = NewDefinitionIndexEx()