package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.DumbModeAccessType
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.index.StringStubIndexWrapper

abstract class AbsIndexBasedChooseByNameContributor<Psi : PsiElement> : ChooseByNameContributorEx {
    protected abstract val index: StringStubIndexWrapper<Psi>

    protected abstract fun createNavigationItem(item: Psi, name: String): NavigationItem?

    final override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        DumbModeAccessType.RELIABLE_DATA_ONLY.ignoreDumbMode {
            index.processAllKeys(scope, filter, processor)
        }
    }

    final override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        DumbModeAccessType.RELIABLE_DATA_ONLY.ignoreDumbMode {
            index.processByName(name, parameters.project, parameters.searchScope, parameters.idFilter) { def ->
                createNavigationItem(def, name)?.let { processor.process(it) } ?: true
            }
        }
    }
}