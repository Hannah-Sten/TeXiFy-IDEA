package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndexKey

abstract class LatexCompositeStubIndex<Psi : PsiElement>(clazz: Class<Psi>) : StringStubIndexWrapper<Psi>(clazz) {

    abstract override fun getKey(): StubIndexKey<String, Psi>

    override fun buildFileset(baseFile: PsiFile): GlobalSearchScope {
        return LatexProjectStructure.getFilesetScopeFor(baseFile)
    }
}

abstract class LatexCompositeTransformedStubIndex<Stub : StubElement<Psi>, Psi : PsiElement>(
    clazz: Class<Psi>
) : LatexCompositeStubIndex<Psi>(clazz), TransformedStubIndex<Stub, Psi>