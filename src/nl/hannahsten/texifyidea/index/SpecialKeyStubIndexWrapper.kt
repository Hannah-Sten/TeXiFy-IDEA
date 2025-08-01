package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink

abstract class SpecialKeyStubIndexWrapper<Psi : PsiElement>(clazz: Class<Psi>) : StringStubIndexWrapper<Psi>(clazz) {
    protected abstract val specialKeys: Set<String>
    protected abstract val specialKeyMap: Map<String, List<String>>

    fun sinkIndex(sink: IndexSink, name: String) {
        val indexKey = key
        specialKeyMap[name]?.let { keys ->
            keys.forEach {
                sink.occurrence(indexKey, it)
            }
        }
    }
}