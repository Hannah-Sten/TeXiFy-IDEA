package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.psi.LatexRawText
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.document
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Hannah Schellekens
 */
abstract class TexifyRegexInspection(

    /**
     * The display name of the inspection.
     */
    val inspectionDisplayName: String,

    /**
     * The short name of the inspection (same name as the html info file).
     */
    override val inspectionId: String,

    /**
     * The regex pattern that targets the text for the inspection.
     */
    val pattern: Pattern,

    /**
     * The error message that shows up when you hover over the problem descriptor.
     */
    val errorMessage: (Matcher) -> String,

    /**
     * What to replace in the document.
     */
    val replacement: (Matcher, PsiFile) -> String = { _, _ -> "" },

    /**
     * Fetches different groups from a matcher.
     */
    val groupFetcher: (Matcher) -> List<String> = { listOf() },

    /**
     * The range in the found pattern that must be replaced.
     */
    val replacementRange: (Matcher) -> IntRange = { it.start()..it.end() },

    /**
     * The highlight level of the problem, GENERIC_ERROR_OR_WARNING by default.
     */
    val highlight: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,

    /**
     * Name of the quick fix.
     */
    val quickFixName: (Matcher) -> String = { "Do fix pls" },

    /**
     * `true` when the inspection is in mathmode, `false` (default) when not in math mode.
     */
    val mathMode: Boolean = false,

    /**
     * Predicate that if `true`, cancels the inspection.
     */
    val cancelIf: (Matcher, PsiFile) -> Boolean = { _, _ -> false },

    /**
     * Provides the text ranges that mark the squiggly warning thingies.
     */
    val highlightRange: (Matcher) -> TextRange = { TextRange(it.start(), it.end()) },

    /**
     * In which inspection inspectionGroup the inspection lies.
     */
    override val inspectionGroup: InsightGroup = InsightGroup.LATEX

) : TexifyInspectionBase() {

    companion object {

        /**
         * Get the IntRange that spans the inspectionGroup with the given id.
         */
        fun Matcher.groupRange(groupId: Int): IntRange = start(groupId)..end(groupId)

        /**
         * Checks if the matched element is a child of a certain PsiElement.
         */
        inline fun <reified T : PsiElement> isInElement(matcher: Matcher, file: PsiFile): Boolean {
            val element = file.findElementAt(matcher.start()) ?: return false
            return element.hasParent(T::class)
        }
    }

    override fun getDisplayName() = inspectionDisplayName

    override fun checkContext(element: PsiElement) = element.isSuppressed().not() && element.firstParentOfType(LatexRawText::class) == null

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): MutableList<ProblemDescriptor> {
        // Find all patterns.
        val text = file.text
        val matcher = pattern.matcher(text)

        return if (!isOntheFly && runForWholeFile()) {
            inspectFileNotOnTheFly(file, manager, matcher)
        }
        else {
            inspectFileOnTheFly(file, manager, matcher)
        }
    }

    /**
     * The file is inspected 'not on the fly' when the 'fix all problems in file' menu is used.
     * If that is the case and we need to run the inspection for the
     * whole file at once, we create only one problem descriptor for
     * the whole file, which has all the problem locations.
     * Then the problem descriptor will be applied and we can take
     * control of handling the displacements of the fixed regex locations ourselves.
     * The reason we do not always want one problem descriptor is
     * that it would be rendered in IntelliJ as one global warning
     * bar at the top of the file, instead of at the correct locations.
     */
    private fun inspectFileNotOnTheFly(file: PsiFile, manager: InspectionManager, matcher: Matcher): MutableList<ProblemDescriptor> {
        val replacementRanges = arrayListOf<IntRange>()
        val replacements = arrayListOf<String>()
        val groups = arrayListOf<List<String>>()

        var quickFixName = ""
        var errorMessage = ""

        // For each pattern, just save the replacement location and value
        // We use the fact that the matcher finds issues in increasing order when applying fixes
        while (matcher.find()) {
            // Pre-checks.
            if (cancelIf(matcher, file)) {
                continue
            }

            groups.add(groupFetcher(matcher))

            val range = replacementRange(matcher)
            val replacementContent = replacement(matcher, file)

            // Just take the last error/name even if it may not apply to all problems at once
            // (they may each have a different error message) because we have to choose only one for the one problem descriptor.
            errorMessage = errorMessage(matcher)
            quickFixName = quickFixName(matcher)

            // Correct context.
            val element = file.findElementAt(matcher.start()) ?: continue
            if (!checkContext(matcher, element)) {
                continue
            }

            replacementRanges.add(range)
            replacements.add(replacementContent)
        }

        if (replacementRanges.isNotEmpty()) {

            // We cannot give one TextRange because there are multiple,
            // but it does not matter since the user won't see this anyway.
            val problemDescriptor = manager.createProblemDescriptor(
                file,
                null as TextRange?,
                errorMessage,
                highlight,
                false,
                RegexFixes(
                    quickFixName,
                    replacements,
                    replacementRanges,
                    groups,
                    this::applyFixes
                )
            )

            return mutableListOf(problemDescriptor)
        }
        else {
            return mutableListOf()
        }
    }

    /**
     * Inspect the file and create a list of all problem descriptors.
     */
    private fun inspectFileOnTheFly(file: PsiFile, manager: InspectionManager, matcher: Matcher): MutableList<ProblemDescriptor> {
        val descriptors = descriptorList()

        while (matcher.find()) {
            // Pre-checks.
            if (cancelIf(matcher, file)) {
                continue
            }

            val groups = groupFetcher(matcher)
            val textRange = highlightRange(matcher)
            val range = replacementRange(matcher)
            val error = errorMessage(matcher)
            val quickFix = quickFixName(matcher)
            val replacementContent = replacement(matcher, file)

            // Correct context.
            val element = file.findElementAt(matcher.start()) ?: continue
            if (!checkContext(matcher, element)) {
                continue
            }

            descriptors.add(
                manager.createProblemDescriptor(
                    file,
                    textRange,
                    error,
                    highlight,
                    true,
                    RegexFixes(
                        quickFix,
                        arrayListOf(replacementContent),
                        arrayListOf(range),
                        arrayListOf(groups),
                        this::applyFixes
                    )
                )
            )
        }

        return descriptors
    }

    /**
     * Checks if the element is in the correct context.
     *
     * By default checks for math mode.
     *
     * @return `true` if the inspection is allowed in the context, `false` otherwise.
     */
    open fun checkContext(matcher: Matcher, element: PsiElement): Boolean {
        if (element.isComment()) {
            return false
        }

        return mathMode == element.inMathContext() && checkContext(element)
    }

    /**
     * We assume the quickfix of this inspection replaces text (like <<) by
     * content with a different length (like \ll), so we have to make
     * sure it is run for the whole file at once.
     * It can be disabled by overriding this method.
     */
    override fun runForWholeFile(): Boolean {
        return true
    }

    /**
     * Replaces all text in the replacementRange by the correct replacement.
     *
     * @return The total increase in document length, e.g. if << is replaced by
     * \ll and \usepackage{amsmath} is added then the total increase is 3 + 20 - 2.
     */
    open fun applyFix(descriptor: ProblemDescriptor, replacementRange: IntRange, replacement: String, groups: List<String>): Int {
        val file = descriptor.psiElement as PsiFile
        val document = file.document() ?: return 0

        document.replaceString(replacementRange.first, replacementRange.last, replacement)

        return replacement.length - replacementRange.length
    }

    /**
     * Replaces all text for all replacementRanges by the correct replacements.
     *
     * We assume the quickfix of an inspection performs replacements which could
     * have a different length than the original text, the inspection
     * should override [runForWholeFile] to explicitly refute this assumption.
     *
     * @param groups Regex groups as matched by the regex matcher.
     * @param replacementRanges These replacement ranges have to be ordered increasingly and have to be non-overlapping.
     */
    open fun applyFixes(
        descriptor: ProblemDescriptor,
        replacementRanges: List<IntRange>,
        replacements: List<String>,
        groups: List<List<String>>
    ) {
        val fixFunction = { replacementRange: IntRange, replacement: String, group: List<String> -> applyFix(descriptor, replacementRange, replacement, group) }
        applyFixes(fixFunction, replacementRanges, replacements, groups)
    }

    /**
     * See [applyFixes].
     */
    open fun applyFixes(
        fixFunction: (IntRange, String, List<String>) -> Int,
        replacementRanges: List<IntRange>,
        replacements: List<String>,
        groups: List<List<String>>
    ) {
        require(replacementRanges.size == replacements.size) { "The number of replacement values has to equal the number of ranges of those replacements." }

        // Remember how much a replacement changed the locations of the fixes still to be applied.
        // This is cumulative, but may be negative.
        var accumulatedDisplacement = 0

        // Loop over all fixes manually, in order to fix the regex locations
        for (i in replacements.indices) {
            val replacementRange = replacementRanges[i]
            val replacement = replacements[i]

            val newRange = IntRange(replacementRange.first + accumulatedDisplacement, replacementRange.last + accumulatedDisplacement)
            val replacementLength = fixFunction(newRange, replacement, groups[i])

            // Fix the locations of the next fixes
            accumulatedDisplacement += replacementLength
        }
    }

    /**
     * @author Hannah Schellekens
     */
    open class RegexFixes(
        private val fixName: String,
        val replacements: List<String>,
        val replacementRanges: List<IntRange>,
        val groups: List<List<String>>,
        val fixFunction: (ProblemDescriptor, List<IntRange>, List<String>, List<List<String>>) -> Unit
    ) : LocalQuickFix {

        override fun getFamilyName(): String = fixName

        override fun applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
            fixFunction(problemDescriptor, replacementRanges, replacements, groups)
        }
    }
}
