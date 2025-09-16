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
import nl.hannahsten.texifyidea.lang.LatexLib
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.psi.LatexNormalText
import nl.hannahsten.texifyidea.util.PackageUtils

/**
 * Base class for inspections that mainly focus on LaTeX commands as anchors.
 *
 * @author Li Ernest
 */
abstract class AbstractTexifyCommandBasedInspection(
    inspectionId: String,
    applicableContexts: LContextSet? = null,
    excludedContexts: LContextSet = emptySet(),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) :
    AbstractTexifyContextAwareInspection(inspectionGroup, inspectionId, applicableContexts, excludedContexts) {

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        // if there cannot be commands inside, do not inspect children
        if (element is LatexNormalText) return false
        if (element is LatexMagicComment) return false
        return true
    }

    override fun inspectElement(element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle, file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>) {
        if (element !is LatexCommands) return
        inspectCommand(element, contexts, bundle, file, manager, isOnTheFly, descriptors)
    }

    /**
     * Inspects a command.
     *
     * It is the caller's responsibility to check the context via [isApplicableInContexts] since some pre-checks can be done more efficiently.
     */
    protected abstract fun inspectCommand(
        command: LatexCommands, contexts: LContextSet,
        defBundle: DefinitionBundle, file: PsiFile,
        manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    )

    protected class ReplaceCommandQuickFix(
        val fixName: String,
        private val newNameWithoutSlash: String,
        private val requiredPkg: LatexLib = LatexLib.BASE
    ) : LocalQuickFix {
        override fun getFamilyName(): @IntentionFamilyName String {
            return fixName
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement as? LatexCommands ?: return
            element.setName(newNameWithoutSlash)
            // Ensure the required package is imported
            if(requiredPkg != LatexLib.BASE) {
                PackageUtils.insertUsePackage(element.containingFile, requiredPkg)
            }
        }
    }
}