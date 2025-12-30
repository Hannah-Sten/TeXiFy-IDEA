package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
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
    skipChildrenInContext: LContextSet = setOf(LatexContexts.Comment),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) : AbstractTexifyContextAwareInspection(inspectionGroup, inspectionId, applicableContexts, excludedContexts, skipChildrenInContext) {

    protected abstract fun errorMessage(matcher: MatchResult, context: LContextSet): String

    protected abstract fun quickFixName(matcher: MatchResult, contexts: LContextSet): String

    protected open fun getHighlightRange(matcher: MatchResult): IntRange = matcher.range

    /**
     * Gets the replacement string for the given match.
     */
    protected abstract fun getReplacement(
        match: MatchResult, fullElementText: String, project: Project, problemDescriptor: ProblemDescriptor
    ): String

    /**
     * Decides whether the regex should try to match the given element.
     *
     * By default, only `LatexTypes.NORMAL_TEXT_WORD` elements are inspected.
     * Also override [shouldInspectChildrenOf] to avoid descending into unwanted elements.
     */
    protected open fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean = element.elementType == LatexTypes.NORMAL_TEXT_WORD

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean = true

    /**
     * Additional checks to be performed after a regex match is found.
     *
     * @return Whether to report the found match.
     */
    protected open fun additionalChecks(
        element: PsiElement, match: MatchResult,
        bundle: DefinitionBundle, file: PsiFile
    ): Boolean = true

    override fun inspectElement(
        element: PsiElement, contexts: LContextSet, bundle: DefinitionBundle,
        file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean, descriptors: MutableList<ProblemDescriptor>
    ) {
        if (!isApplicableInContexts(contexts)) return
        if (!shouldInspectElement(element, bundle)) return
        val elementText = element.text
        if (elementText.isEmpty()) return
        for (match in regex.findAll(elementText)) {
            if (!additionalChecks(element, match, bundle, file)) continue
            val highlightRange = getHighlightRange(match)
            if (highlightRange.isEmpty() || !match.range.contains(highlightRange)) continue
            val textRange = highlightRange.toTextRange()

            val error = errorMessage(match, contexts)
            val quickFix = quickFixName(match, contexts)
            val fix = RegexFix(quickFix, match, elementText)

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
        project: Project, descriptor: ProblemDescriptor, match: MatchResult, fullElementText: String
    ) {
        val element = descriptor.psiElement
        val document = element.containingFile.document() ?: return
        val repRange = match.range.toTextRange().shiftRight(element.startOffset)
        val rep = getReplacement(match, fullElementText, project, descriptor)
        document.replaceString(repRange, rep)
    }

    /**
     * Generates the preview for a single replacement.
     *
     * Override when overriding [doApplyFix].
     */
    protected open fun doGeneratePreview(
        project: Project, descriptor: ProblemDescriptor, match: MatchResult, fullElementText: String
    ): IntentionPreviewInfo {
        val replacement = getReplacement(match, fullElementText, project, descriptor)
        return IntentionPreviewInfo.CustomDiff(LatexFileType, match.value, replacement)
    }

    /**
     * A local quick fix capable of applying one or multiple regex replacements.
     */
    protected inner class RegexFix(
        private val fixName: String, val match: MatchResult, val fullElementText: String
    ) : LocalQuickFix {

        override fun getFamilyName(): String = fixName

        override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
            doApplyFix(project, problemDescriptor, match, fullElementText)
        }

        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = doGeneratePreview(project, previewDescriptor, match, fullElementText)
    }
}

/**
 * An adaptor class for the existing regex-based inspections that scan the whole file.
 *
 * Note that they can be messy.
 */
abstract class AbstractTexifyWholeFileRegexBasedInspection(
    inspectionId: String, regex: Regex,
    highlight: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    applicableContexts: LContextSet? = null,
    excludedContexts: LContextSet = emptySet(),
    skipChildrenInContext: LContextSet = setOf(LatexContexts.Comment),
    inspectionGroup: InsightGroup = InsightGroup.LATEX,
) : AbstractTexifyRegexBasedInspection(
    inspectionId,
    regex,
    highlight,
    applicableContexts,
    excludedContexts,
    skipChildrenInContext,
    inspectionGroup
) {

    override fun shouldInspectElement(element: PsiElement, lookup: LatexSemanticsLookup): Boolean {
        return element is PsiFile // Only inspect the whole file
    }

    override fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        return false // so that we don't descend into children
    }

    override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
        // we do not scan for local suppressions for performance reasons (which can be costly in large files)
        // so we only offer to suppress the whole file
        element ?: return SuppressQuickFix.EMPTY_ARRAY
        val file = element.containingFile ?: return SuppressQuickFix.EMPTY_ARRAY
        return arrayOf(FileSuppressionFix(file.createSmartPointer()))
    }
}