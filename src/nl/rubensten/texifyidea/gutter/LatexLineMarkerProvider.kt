package nl.rubensten.texifyidea.gutter

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement

/**
 * @author Sten Wessel
 */
open class LatexLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(element: PsiElement,
                                          result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>?) {
        LatexNavigationGutter().collectNavigationMarkers(element, result)
    }
}
