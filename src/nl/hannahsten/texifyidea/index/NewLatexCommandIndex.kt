package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.LatexStructure
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class NewCommandsIndexEx : NewLatexCompositeStubIndex<LatexCommands>(LatexCommands::class.java) {

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS
    }
}

/**
 * The index for all commands.
 *
 *
 */
val NewCommandsIndex = NewCommandsIndexEx()

/**
 * Definitions of both commands and theorems
 */
class NewDefinitionIndexEx : NewLatexCompositeTransformedStubIndex<LatexCommandsStub, LatexCommands>(LatexCommands::class.java) {
    override fun getVersion(): Int {
        return 1003
    }

    override fun sinkIndex(stub: LatexCommandsStub, sink: IndexSink) {
        val command = stub.commandToken
        if (command !in CommandMagic.definitions) return
        LatexStructure.getDefinedCommandName(stub)?.let {
            sink.occurrence(key, it)
        }
    }

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.DEFINITIONS
    }
}

val NewDefinitionIndex = NewDefinitionIndexEx()
