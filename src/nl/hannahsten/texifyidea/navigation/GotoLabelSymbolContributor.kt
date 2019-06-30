package nl.hannahsten.texifyidea.navigation

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import nl.hannahsten.texifyidea.util.extractLabelName
import nl.hannahsten.texifyidea.util.findLabels

/**
 * @author Hannah Schellekens
 */
class GotoLabelSymbolContributor : TexifyGotoSymbolBase<PsiElement>() {

    override fun Project.findElements() = findLabels()

    override fun PsiElement.extractName() = extractLabelName()

    override fun PsiElement.createNavigationItem() = NavigationItemUtil.createLabelNavigationItem(this)
}