package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.PackageUtils

/**
 * Base class for inspections that mainly focus on LaTeX environments as anchors.
 *
 * @author Ezrnest
 */
abstract class AbstractTexifyEnvironmentBasedInspection(
    inspectionId: String,
    applicableContexts: LContextSet? = null,
    excludedContexts: LContextSet = emptySet(),
    skipChildrenInContext: LContextSet = setOf(LatexContexts.Comment),
) :
    AbstractTexifyContextAwareInspection(InsightGroup.LATEX, inspectionId, applicableContexts, excludedContexts, skipChildrenInContext) {

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        // if there cannot be environments inside, do not inspect children
        if (element is LatexNormalText) return false
        if (element is LatexMagicComment) return false
        return true
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if (element !is LatexEnvironment) return
        inspectEnvironment(element, contexts, bundle, file, manager, isOnTheFly, descriptors)
    }

    /**
     * Inspects an environment.
     *
     * It is the caller's responsibility to check the context via [isApplicableInContexts] since some pre-checks can be done more efficiently.
     */
    protected abstract fun inspectEnvironment(
        environment: LatexEnvironment, contexts: LContextSet,
        defBundle: DefinitionBundle, file: PsiFile,
        manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    )

    protected class ReplaceEnvironmentQuickFix(
        val fixName: String,
        private val newName: String,
        private val requiredPkg: LatexLib = LatexLib.BASE
    ) : LocalQuickFix {
        override fun getFamilyName(): @IntentionFamilyName String {
            return fixName
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as? LatexEnvironment ?: return
            // Ensure the required package is imported
            element.beginCommand.envIdentifier?.setName(newName)
            element.endCommand?.envIdentifier?.setName(newName)
            if(requiredPkg != LatexLib.BASE) {
                PackageUtils.insertUsePackage(element.containingFile, requiredPkg)
            }
        }
    }
}
