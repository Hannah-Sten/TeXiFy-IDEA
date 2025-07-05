package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

/**
 * @author Hannah Schellekens
 */
abstract class TexifyGotoSymbolBase<Psi> : ChooseByNameContributorEx {

    /**
     * Finds all elements that can be found in 'Goto symbol'.
     */
    abstract fun processElements(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<Psi>)

    /**
     * Transforms the element into its visible and identifyable name.
     * `null` when no name is available.
     */
    abstract fun extractName(item: Psi): String?

    /**
     * Creates a navigation item from the given element.
     * `null` when no navigation item could be created.
     */
    abstract fun createNavigationItem(item: Psi): NavigationItem?

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        processElements(scope, filter) {
            extractName(it)?.let { processor.process(it) } ?: true
        }
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        processElements(parameters.searchScope, parameters.idFilter) {
            val defName = extractName(it) ?: return@processElements true
            if(defName != name) return@processElements true
            processor.process(createNavigationItem(it))
        }
    }
}