package nl.rubensten.texifyidea.navigation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.rubensten.texifyidea.util.extractLabelName
import nl.rubensten.texifyidea.util.findLabels

/**
 * @author Ruben Schellekens
 */
class GotoLabelSymbolContributor : TexifyGotoSymbolBase<PsiElement>() {

    override fun Project.findElements() = findLabels()

    override fun PsiElement.extractName() = extractLabelName()

    override fun PsiElement.createNavigationItem() = NavigationItemUtil.createLabelNavigationItem(this)
}