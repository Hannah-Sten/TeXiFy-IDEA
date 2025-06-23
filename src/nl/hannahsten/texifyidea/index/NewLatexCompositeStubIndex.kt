package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubIndexKey
import nl.hannahsten.texifyidea.index.stub.LatexCommandsStub
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.files.documentClassFileInProject
import nl.hannahsten.texifyidea.util.files.findRootFiles
import nl.hannahsten.texifyidea.util.files.referencedFileSet

fun buildLatexSearchFiles(baseFile: PsiFile): GlobalSearchScope {
    // TODO: improve it, very slow
    val useIndexCache = true
    val searchFiles = baseFile.referencedFileSet(useIndexCache)
        .mapNotNullTo(mutableSetOf()) { it.virtualFile }
    searchFiles.add(baseFile.virtualFile)

    // Add document classes
    // There can be multiple, e.g., in the case of subfiles, in which case we probably want all items in the super-fileset
    val roots = baseFile.findRootFiles()
    for (root in roots) {
        val docClass = root.documentClassFileInProject() ?: continue
        searchFiles.add(docClass.virtualFile)
        docClass.referencedFileSet(useIndexCache).forEach {
            searchFiles.add(it.virtualFile)
        }
    }

    // Search index.
    return GlobalSearchScope.filesScope(baseFile.project, searchFiles)
}

abstract class NewLatexCompositeStubIndex<Psi : PsiElement>(clazz: Class<Psi>) : MyStringStubIndexBase<Psi>(clazz) {

    abstract override fun getKey(): StubIndexKey<String, Psi>

    override fun wrapSearchScope(scope: GlobalSearchScope): GlobalSearchScope {
        return LatexFileFilterScope(scope)
    }

    override fun buildSearchFiles(baseFile: PsiFile): GlobalSearchScope {
        return buildLatexSearchFiles(baseFile)
    }
}

abstract class NewLatexCompositeTransformedStubIndex<Stub : StubElement<Psi>, Psi : PsiElement>(
    clazz: Class<Psi>
) : NewLatexCompositeStubIndex<Psi>(clazz), MyTransformedStubIndex<Stub, Psi> {
}