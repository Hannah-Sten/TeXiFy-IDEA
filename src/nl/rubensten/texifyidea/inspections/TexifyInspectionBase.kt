package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.lang.magic.allParentMagicComments
import nl.rubensten.texifyidea.lang.magic.containsPair
import nl.rubensten.texifyidea.util.isComment

/**
 * @author Ruben Schellekens
 */
abstract class TexifyInspectionBase : LocalInspectionTool() {

    /**
     * The inspectionGroup the inspection falls under.
     */
    abstract val inspectionGroup: InsightGroup

    /**
     * A unique string indentifier for the inspection.
     */
    abstract val inspectionId: String

    /**
     * Gets called whenever the file should be inspected.
     *
     * @param file
     *          The file to inspect.
     * @param manager
     *          InspectionManager to ask for ProblemDescriptor's from.
     * @param isOntheFly
     *          `true` if called during on the fly editor highlighting. Called from Inspect Code action otherwise.
     */
    abstract fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor>

    /**
     * Checks if the element is in the correct context for the inspection to be applied.
     *
     * @return `true` if the inspection is allowed for this element in its context, `false` otherwise.
     */
    open fun checkContext(element: PsiElement) = element.isComment().not() && element.isSuppressed().not()

    /**
     * Creates an empty list to store problem descriptors in.
     */
    protected open fun descriptorList(): MutableList<ProblemDescriptor> = SmartList()

    /**
     * Checks whether the inspection must be suppressed (`true`) or not (`false`) based on the position of the given
     * PsiElement.
     */
    protected open fun PsiElement.isSuppressed(): Boolean {
        return allParentMagicComments().containsPair("suppress", inspectionId)
    }

    override fun getShortName() = inspectionGroup.prefix + inspectionId

    override fun getGroupDisplayName() = inspectionGroup.displayName

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        // Only inspect the right file types.
        if (file.fileType !in inspectionGroup.fileTypes) {
            return null
        }

        // Check for file inspection suppression seperately as it is relatively cheap.
        // Do not execute the (relative expensive) inspection when it is suppressed globally.
        if (file.isSuppressed()) {
            return null
        }

        return inspectFile(file, manager, isOnTheFly)
                .filter { checkContext(it.psiElement) }
                .toTypedArray()
    }
}