package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem

/**
 * @author Hannah Schellekens
 */
abstract class TexifyGotoSymbolBase<T> : ChooseByNameContributorEx {

// //    /**
// //     * Finds all elements that can be found in 'Goto symbol'.
// //     */
// //    abstract fun Project.findElements(): Iterable<T>
//
//    /**
//     * Transforms the element into its visible and identifyable name.
//     * `null` when no name is available.
//     */
//    abstract fun T.extractName(): String?

    /**
     * Creates a navigation item from the given element.
     * `null` when no navigation item could be created.
     */
    abstract fun createNavigationItem(item: T): NavigationItem?

//    abstract override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?)
//
//    abstract override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters)

//    override fun getItemsByName(name: String?, pattern: String?, project: Project?, includeNonProjectItems: Boolean): Array<NavigationItem> {
//        val elements = project?.findElements() ?: return emptyArray()
//        return elements.asSequence()
//            .map { it to it.extractName() }
//            .filter { (_, definedName) ->
//                definedName == name || (pattern != null && (definedName ?: "").contains(pattern, ignoreCase = true))
//            }
//            .mapNotNull { (psi, _) -> psi.createNavigationItem() }
//            .toList()
//            .toTypedArray()
//    }
//
//    override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
//        val elements = project?.findElements() ?: return emptyArray()
//        return elements.mapNotNull { it.extractName() }.toTypedArray()
//    }
}