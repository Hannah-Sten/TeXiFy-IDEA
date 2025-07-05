package nl.hannahsten.texifyidea.navigation

import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.IdFilter
import nl.hannahsten.texifyidea.util.labels.extractLabelName

/**
 * @author Hannah Schellekens
 */
class GotoLabelSymbolContributor : TexifyGotoSymbolBase<PsiElement>() {

//    override fun Project.findElements() = findAllLabelsAndBibtexIds()
//
//    override fun PsiElement.extractName() = extractLabelName()
//
//    override fun PsiElement.createNavigationItem() = NavigationItemUtil.createLabelNavigationItem(this)

    override fun createNavigationItem(item: PsiElement): NavigationItem? {
        return NavigationItemUtil.createLabelNavigationItem(item)
    }

    override fun processElements(scope: GlobalSearchScope, filter: IdFilter?, processor: Processor<PsiElement>) {
        // TODO
    }

    override fun extractName(item: PsiElement): String? {
        return item.extractLabelName()
    }
}