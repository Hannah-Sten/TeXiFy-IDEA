package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.lang.LContextSet

abstract class TexifyContextAwareRegexInspectionBase(
    inspectionId: String,
    val regex: Regex,
    val highlight: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    /**
     * If null, applies to all contexts except those in [excludedContexts].
     * If non-null, applies only to those contexts, except those in [excludedContexts].
     */
    val applicableContexts: LContextSet? = null,
    /**
     * The contexts in which this inspection should not be applied.
     */
    val excludedContexts: LContextSet = emptySet(),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) : TexifyContextAwareInspectionBase(inspectionGroup, inspectionId) {

    protected abstract fun errorMessage(matcher: MatchResult, file: PsiFile): String

    protected abstract fun replacement(matcher: MatchResult, file: PsiFile): String

    protected abstract fun quickFixName(matcher: MatchResult, file: PsiFile): String

    protected open fun replacementRange(matcher: MatchResult): IntRange {
        return matcher.range
    }

    override fun inspectElement(
        element: PsiElement, contexts: LContextSet, manager: InspectionManager,
        isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
    }
}