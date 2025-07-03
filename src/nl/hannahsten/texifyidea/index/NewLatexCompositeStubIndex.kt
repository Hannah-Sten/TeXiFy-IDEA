package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndexKey

abstract class NewLatexCompositeStubIndex<Psi : PsiElement>(clazz: Class<Psi>) : MyStringStubIndexBase<Psi>(clazz) {

    abstract override fun getKey(): StubIndexKey<String, Psi>

    override fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return LatexFileFilterScope(scope)
    }

    override fun buildFileset(baseFile: PsiFile): GlobalSearchScope {
        return LatexProjectStructure.buildFilesetScope(baseFile)
    }
}

abstract class NewLatexCompositeTransformedStubIndex<Stub : StubElement<Psi>, Psi : PsiElement>(
    clazz: Class<Psi>
) : NewLatexCompositeStubIndex<Psi>(clazz), MyTransformedStubIndex<Stub, Psi>