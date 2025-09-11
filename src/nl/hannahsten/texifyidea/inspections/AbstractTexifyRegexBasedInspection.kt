package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document

/**
 * A regex-based inspection for plain text contents.
 */
abstract class AbstractTexifyRegexBasedInspection(
    inspectionId: String,
    val regex: Regex,
    val highlight: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts: LContextSet? = null,
    excludedContexts: LContextSet = emptySet(),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) : AbstractTexifyContextAwareInspection(inspectionGroup, inspectionId, applicableContexts, excludedContexts) {

    protected abstract fun errorMessage(matcher: MatchResult): String

    protected abstract fun quickFixName(matcher: MatchResult): String

    protected open fun getHighlightRange(matcher: MatchResult): IntRange {
        return matcher.range
    }

    /**
     * Gets the replacement string for the given match.
     */
    protected abstract fun getReplacement(
        match: MatchResult, project: Project, problemDescriptor: ProblemDescriptor
    ): String

    /**
     * Decides whether the regex should try to match the given element.
     *
     * By default, only `LatexTypes.NORMAL_TEXT_WORD` elements are inspected.
     * Also override [shouldInspectChildrenOf] to avoid descending into unwanted elements.
     */
    protected open fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        return element.elementType == LatexTypes.NORMAL_TEXT_WORD
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        return true
    }

    /**
     * Additional checks to be performed after a regex match is found.
     *
     * @return Whether to report the found match.
     */
    protected open fun additionalChecks(element: PsiElement, match: MatchResult): Boolean {
        return true
    }

    override fun inspectElement(
        element: PsiElement, contexts: LContextSet, lookup: DefinitionBundle,
        manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        if (!isApplicableInContexts(contexts)) return
        if (!shouldInspectElement(element, lookup)) return
        val elementText = element.text
        if (elementText.isEmpty()) return
        if (!regex.containsMatchIn(elementText)) return
        for (match in regex.findAll(elementText)) {
            val matchText = match.value
            if (!additionalChecks(element, match)) continue
            val highlightRange = getHighlightRange(match)
            if (highlightRange.isEmpty() || !match.range.contains(highlightRange)) continue
            val textRange = highlightRange.toTextRange()

            val error = errorMessage(match)
            val quickFix = quickFixName(match)
            val fix = RegexFix(quickFix, match)

            descriptors.add(
                manager.createProblemDescriptor(
                    element,
                    textRange,
                    error,
                    highlight,
                    true,
                    fix
                )
            )
        }
    }

    /**
     *
     */
    protected open fun doApplyFix(
        project: Project, descriptor: ProblemDescriptor, match: MatchResult
    ): Int {
        val element = descriptor.psiElement
        val document = element.containingFile.document() ?: return 0
        val repRange = match.range.toTextRange().shiftRight(element.startOffset)
        val rep = getReplacement(match, project, descriptor)
        document.replaceString(repRange, rep)
        return rep.length - match.value.length
    }

    /**
     * Generates the preview for a single replacement.
     *
     * Override when overriding [doApplyFix].
     */
    protected open fun doGeneratePreview(
        project: Project, descriptor: ProblemDescriptor, match: MatchResult,
    ): IntentionPreviewInfo {
        val replacement = getReplacement(match, project, descriptor)
        return IntentionPreviewInfo.CustomDiff(LatexFileType, match.value, replacement)
    }

    /**
     * A local quick fix capable of applying one or multiple regex replacements.
     */
    protected inner class RegexFix(
        private val fixName: String, val match: MatchResult
    ) : LocalQuickFix {

        override fun getFamilyName(): String = fixName

        override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
            doApplyFix(project, problemDescriptor, match)
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            return doGeneratePreview(project, previewDescriptor, match)
        }
    }
}