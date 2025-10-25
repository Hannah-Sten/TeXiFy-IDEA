package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.parser.LatexPsiUtil
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class NewCommandsIndexEx : LatexCompositeStubIndex<LatexCommands>(LatexCommands::class.java) {

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS
    }

    override fun getVersion(): Int {
        return 2
    }
}

/**
 * The index for all commands.
 */
val NewCommandsIndex = NewCommandsIndexEx()

class NewLatexEnvironmentIndexEx : LatexCompositeStubIndex<LatexEnvironment>(LatexEnvironment::class.java) {

    override fun getKey(): StubIndexKey<String, LatexEnvironment> {
        return LatexStubIndexKeys.ENVIRONMENTS
    }

    override fun getVersion(): Int {
        return 2
    }
}

/**
 * The index for all environments.
 */
val NewLatexEnvironmentIndex = NewLatexEnvironmentIndexEx()

/**
 * Definitions of both commands and theorems
 */
class NewDefinitionIndexEx : LatexCompositeTransformedStubIndex<LatexCommandsStub, LatexCommands>(LatexCommands::class.java) {
    override fun getVersion(): Int {
        return 1004
    }

    override fun sinkIndex(stub: LatexCommandsStub, sink: IndexSink) {
        val command = stub.commandToken
        if (command !in CommandMagic.definitions) return
        if(stub.parentStub !is PsiFileStub) return
        LatexPsiUtil.getDefinedCommandName(stub)?.let {
            sink.occurrence(key, it)
        }
    }

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.DEFINITIONS
    }
}

val NewDefinitionIndex = NewDefinitionIndexEx()