package nl.hannahsten.texifyidea.index

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class NewCommandsIndexEx : NewLatexCompositeStubIndex<LatexCommands>(LatexCommands::class.java) {

    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.COMMANDS
    }
}

val NewCommandsIndex = NewCommandsIndexEx()

/**
 * Definitions of both commands and theorems
 */
class NewDefinitionIndexEx : NewLatexCompositeTransformedStubIndex<LatexCommandsStub, LatexCommands>(LatexCommands::class.java) {
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
        return LatexStubIndexKeys.DEFINITIONS
    }
}

val NewDefinitionIndex = NewDefinitionIndexEx()


class NewLabelsIndexEx : NewLatexCompositeTransformedStubIndex<StubElement<LatexComposite>, LatexComposite>(LatexComposite::class.java) {
    override fun getVersion(): Int {
        return 101
    }

    override fun getKey(): StubIndexKey<String, LatexComposite> {
        return LatexStubIndexKeys.LABELED_ELEMENT
    }

    override fun sinkIndex(stub: StubElement<LatexComposite>, sink: IndexSink) {
        when (stub) {
            is LatexCommandsStub -> {
                val command = stub.commandToken
                if (command in CommandMagic.labelReferenceWithoutCustomCommands) {
                    sink.occurrence(key, stub.requiredParams[0])
                }
            }

            is LatexEnvironmentStub -> {

            }
        }
    }
}