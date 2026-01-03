package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStub
import nl.hannahsten.texifyidea.index.stub.requiredParamAt
import nl.hannahsten.texifyidea.psi.LatexComposite
import nl.hannahsten.texifyidea.util.magic.CommandMagic

class NewLabelsIndexEx : LatexCompositeTransformedStubIndex<StubElement<LatexComposite>, LatexComposite>(LatexComposite::class.java) {
    override fun getVersion(): Int = 102

    override fun getKey(): StubIndexKey<String, LatexComposite> = LatexStubIndexKeys.LABELED_ELEMENT

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
            stub.requiredParamAt(0)?.let {
                // It is possible that the command has no required parameters, e.g. `\label`, which is often used in class files.
                sink.occurrence(key, it)
            }
        }
        else if (command in CommandMagic.labelAsParameter) {
            stub.optionalParamsMap["label"]?.let { label ->
                sink.occurrence(key, label)
            }
        }
    }

    fun sinkIndexEnv(stub: LatexEnvironmentStub, sink: IndexSink) {
        stub.label?.let { sink.occurrence(key, it) }
    }

    @Suppress("unused")
    fun getAllLabels(project: Project): Set<String> = getAllKeys(project)

    fun getAllLabels(filesetScope: GlobalSearchScope): Set<String> = getAllKeys(filesetScope)
}

/**
 * The index of labeled elements, which includes both commands and environments.
 */
val NewLabelsIndex = NewLabelsIndexEx()