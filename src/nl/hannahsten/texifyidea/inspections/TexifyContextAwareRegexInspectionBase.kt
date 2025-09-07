package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import kotlin.math.max

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
     */
    val excludedContexts: LContextSet = emptySet(),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) : TexifyContextAwareInspectionBase(inspectionGroup, inspectionId) {

    protected abstract fun errorMessage(matcher: MatchResult, file: PsiFile): String

    protected abstract fun getReplacement(matcher: MatchResult, file: PsiFile): String

    protected open fun quickFixName(matcher: MatchResult, file: PsiFile): String {
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
    protected open fun shouldInspectElement(element: PsiElement): Boolean {
        return element.elementType == LatexTypes.NORMAL_TEXT_WORD
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet): Boolean {
        return true
    }

    override fun inspectElement(
        element: PsiElement, contexts: LContextSet, manager: InspectionManager,
        isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        if (!isApplicableInContexts(contexts)) return
        if (!shouldInspectElement(element)) return
        val text = element.text
        if (text.isEmpty()) return
        if (!regex.containsMatchIn(text)) return

        val file = element.containingFile ?: return
        for (match in regex.findAll(text)) {
            val textRange = getHighlightRange(match).toTextRange()
            if (textRange.isEmpty || textRange.startOffset < 0 || textRange.endOffset > text.length) continue

            val rangeLocal = getReplacementRange(match)
            val error = errorMessage(match, file)
            val quickFix = quickFixName(match, file)
            val replacementContent = getReplacement(match, file)
            val groupValues = groups(match)

            descriptors.add(
                manager.createProblemDescriptor(
                    element,
                    textRange,
                    error,
                    highlight,
                    true,
                    RegexFixes(
                        quickFix,
                        arrayListOf(replacementContent),
                        arrayListOf(rangeLocal),
                        arrayListOf(groupValues)
                    )
                )
            )
        }
    }

    /**
     * Replaces all text in the [replacementRange] by the correct replacement.
     *
     * When overriding this, probably also override [generatePreview] to fix the intention preview.
     *
     * @return The total increase in document length, e.g. if << is replaced by
     * \\ll and \\usepackage{amsmath} is added then the total increase is 3 + 20 - 2.
     */
    open fun applyFix(
        descriptor: ProblemDescriptor,
        replacementRange: IntRange,
        replacement: String,
        groups: List<String>
    ): Int {
        val element = descriptor.psiElement
        val document = element.containingFile.document() ?: return 0
        val elementStart = element.startOffset
        val repRange = replacementRange.toTextRange().shiftRight(elementStart)
        document.replaceString(repRange, replacement)
        return replacement.length - repRange.length
    }

    /**
     * Generates the preview of applying the quick fix of the element at the cursor.
     */
    fun generatePreview(
        project: Project,
        descriptor: ProblemDescriptor,
        replacementRanges: List<IntRange>,
        replacements: List<String>,
        groups: List<List<String>>
    ): IntentionPreviewInfo {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return IntentionPreviewInfo.EMPTY
        // +1 because the caret seems to always be at the start of the text highlighted in the inspection.
        // Take the first replacement as best guess default.
        val replacementIndex = max(replacementRanges.indexOfFirst { it.contains(editor.caretOffset() + 1) }, 0)
        return generatePreview(project, descriptor, replacementRanges[replacementIndex], replacements[replacementIndex], groups[replacementIndex])
    }

    /**
     * Generates the preview for a single replacement.
     *
     * Override when overriding [applyFix].
     */
    fun generatePreview(
        project: Project,
        descriptor: ProblemDescriptor,
        replacementRange: IntRange,
        replacement: String,
        groups: List<String>
    ): IntentionPreviewInfo {
        val original = descriptor.psiElement.containingFile.text.substring(replacementRange)
        return IntentionPreviewInfo.CustomDiff(LatexFileType, original, replacement)
    }

    open fun applyFixes(
        descriptor: ProblemDescriptor,
        replacementRanges: List<IntRange>,
        replacements: List<String>,
        groups: List<List<String>>
    ) {
        require(replacementRanges.size == replacements.size) { "The number of replacement values has to equal the number of ranges of those replacements." }
        var accumulatedDisplacement = 0
        for (i in replacements.indices) {
            val replacementRange = replacementRanges[i]
            val replacement = replacements[i]
            val newRange = IntRange(replacementRange.first + accumulatedDisplacement, replacementRange.last + accumulatedDisplacement)
            val replacementLength = applyFix(descriptor, newRange, replacement, groups[i])
            accumulatedDisplacement += replacementLength
        }
    }

    /**
     * A local quick fix capable of applying one or multiple regex replacements.
     */
    inner class RegexFixes(
        private val fixName: String,
        val replacements: List<String>,
        val replacementRanges: List<IntRange>,
        val groups: List<List<String>>
    ) : LocalQuickFix {

        override fun getFamilyName(): String = fixName

        override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
            applyFixes(problemDescriptor, replacementRanges, replacements, groups)
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
            return generatePreview(project, previewDescriptor, replacementRanges, replacements, groups)
        }
    }
}