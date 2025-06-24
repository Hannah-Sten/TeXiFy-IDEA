package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.util.magic.CommandMagic

/**
 * The index of labeled elements, which includes both commands and environments.
 */
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
                sinkIndexCommand(stub, sink)
            }
            is LatexEnvironmentStub -> {
                sinkIndexEnv(stub, sink)
            }
        }
    }

    fun sinkIndexCommand(stub: LatexCommandsStub, sink: IndexSink) {
        val command = stub.commandToken
        if (command in CommandMagic.labels) {
            sink.occurrence(key, stub.requiredParams[0])
        }
    }

    fun sinkIndexEnv(stub: LatexEnvironmentStub, sink: IndexSink) {
        stub.label?.let { sink.occurrence(key, it) }
    }

    fun getAllLabels(project: Project): Set<String> {
        return getAllKeys(project)
    }
}

val NewLabelsIndex = NewLabelsIndexEx()

class NewLabelRefIndexEx : NewLatexCompositeTransformedStubIndex<LatexCommandsStub, LatexCommands>(LatexCommands::class.java) {
    override fun getKey(): StubIndexKey<String, LatexCommands> {
        return LatexStubIndexKeys.LABEL_REFERENCE
    }

    override fun sinkIndex(stub: LatexCommandsStub, sink: IndexSink) {
        TODO("Not yet implemented")
    }
}