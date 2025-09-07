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
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document

/**
 * A regex-based inspection for plain text contents.
 */
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
     *
     * Note that comment is always excluded.
     */
    val excludedContexts: LContextSet = emptySet(),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) : TexifyContextAwareInspectionBase(inspectionGroup, inspectionId) {

    protected abstract fun errorMessage(matcher: MatchResult): String

    protected abstract fun getReplacement(matcher: MatchResult): String

    protected open fun quickFixName(matcher: MatchResult): String {
        return "Do fix please"
    }

    protected open fun getReplacementRange(matcher: MatchResult): IntRange {
        return matcher.range
    }

    protected open fun getHighlightRange(matcher: MatchResult): IntRange {
        return matcher.range
    }

    /**
     * Optional groups provider, defaults to returning all captured groups (excluding the full match at index 0).
     */
    protected open fun groups(matcher: MatchResult): List<String> = matcher.groupValues.drop(1)

    /**
     * Whether this element should be inspected under the given contexts.
     */
    protected fun isApplicableInContexts(contexts: LContextSet): Boolean {
        if (contexts.any { it in excludedContexts }) return false
        val app = applicableContexts ?: return true
        return contexts.any { it in app }
    }

    /**
     * By default, only inspect leaf elements to avoid duplicate matches across overlapping PSI nodes.
     */
    protected open fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        return element.elementType == LatexTypes.NORMAL_TEXT_WORD
    }

    protected open fun additionalChecks(element: PsiElement): Boolean {
        return true
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        return true
    }

    override fun inspectElement(
        element: PsiElement, contexts: LContextSet, lookup: LatexSemanticsLookup,
        manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        if (!isApplicableInContexts(contexts)) return
        if (!shouldInspectElement(element, lookup)) return
        val text = element.text
        if (text.isEmpty()) return
        if (!regex.containsMatchIn(text)) return
        for (match in regex.findAll(text)) {
            val textRange = getHighlightRange(match).toTextRange()
            if (textRange.isEmpty || textRange.startOffset < 0 || textRange.endOffset > text.length) continue

            val rangeLocal = getReplacementRange(match)
            val error = errorMessage(match)
            val quickFix = quickFixName(match)
            val replacementContent = getReplacement(match)
            val fix = RegexFix(quickFix, rangeLocal, replacementContent, match)

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
     * Replaces all text in the [replacementRange] by the correct replacement.
     *
     * When overriding this, probably also override [doGeneratePreview] to fix the intention preview.
     *
     * @return The total increase in document length, e.g. if << is replaced by
     * \\ll and \\usepackage{amsmath} is added then the total increase is 3 + 20 - 2.
     */
    protected open fun doApplyFix(
        project: Project, descriptor: ProblemDescriptor, regexFix: RegexFix,
    ): Int {
        val replacementRange = regexFix.replacementRange
        val replacement = regexFix.replacement
        val element = descriptor.psiElement
        val document = element.containingFile.document() ?: return 0
        val elementStart = element.startOffset
        val repRange = replacementRange.toTextRange().shiftRight(elementStart)
        document.replaceString(repRange, replacement)
        return replacement.length - replacementRange.length
    }

    /**
     * Generates the preview for a single replacement.
     *
     * Override when overriding [doApplyFix].
     */
    protected open fun doGeneratePreview(
        project: Project, descriptor: ProblemDescriptor, regexFix: RegexFix,
    ): IntentionPreviewInfo {
        val replacementRange = regexFix.replacementRange
        val replacement = regexFix.replacement
        val original = descriptor.psiElement.containingFile.text.substring(replacementRange)
        return IntentionPreviewInfo.CustomDiff(LatexFileType, original, replacement)
    }

    /**
     * A local quick fix capable of applying one or multiple regex replacements.
     */
    protected inner class RegexFix(
        private val fixName: String,
        val replacementRange: IntRange,
        val replacement: String,
        val matcher: MatchResult
    ) : LocalQuickFix {

        override fun getFamilyName(): String = fixName

        override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
            doApplyFix(project, problemDescriptor, this)
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            return doGeneratePreview(project, previewDescriptor, this)
        }
    }
}