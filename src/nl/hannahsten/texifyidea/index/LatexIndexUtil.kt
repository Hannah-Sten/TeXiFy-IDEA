package nl.hannahsten.texifyidea.index

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import nl.hannahsten.texifyidea.highlighting.LatexAnnotator

fun <T : PsiElement> indexSinkOccurrence(indexSink: IndexSink, util: IndexUtilBase<T>, token: String) {
    // Clear cache to be sure that any update will be reflected (we don't know whether something will be added to the index or whether it's already in there)
//    util.cache.clear() // Temporarily don't clear cache because the index is not reliable: #4006
    indexSink.occurrence(util.key(), token)
    LatexAnnotator.Cache.allUserDefinedCommands = emptyList()
}