package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.elementType
import com.intellij.util.SmartList
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.DefinitionBundle
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase.Companion.suppressionElement
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContextIntro
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.lang.magic.MutableMagicComment
import nl.hannahsten.texifyidea.lang.magic.addMagicCommentToPsiElement
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexTypes
import nl.hannahsten.texifyidea.psi.LatexWithContextTraverser
import nl.hannahsten.texifyidea.psi.containsKeyValuePair
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.existsIntersection
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.traverse

/**
 * Base class for context-aware inspections.
 * The inspection will traverse the whole file, and compute the contexts along the way.
 *
 * @author Ezrnest
 */
abstract class AbstractTexifyContextAwareInspection(
    /**
     * The inspectionGroup the inspection falls under.
     */
    val inspectionGroup: InsightGroup,
    /**
     * A unique string identifier for the inspection.
     */
    val inspectionId: String,
    /**
     * If null, applies to all contexts except those in [excludedContexts].
     * If non-null, applies only to those contexts, except those in [excludedContexts].
     */
    val applicableContexts: LContextSet?,
    /**
     * The contexts in which this inspection should not be applied.
     *
     * Note that comment is always excluded.
     */
    val excludedContexts: LContextSet,
    /**
     * The contexts in which all the children should not be skipped.
     * By default, children inside comments are skipped.
     *
     * For example, "reference not found" inspection should additionally skip children inside definitions.
     */
    val skipChildrenInContext: LContextSet
) : LocalInspectionTool() {

    /**
     * The magic comment scopes that should not have a [SuppressQuickFix].
     */
    open val ignoredSuppressionScopes: Set<MagicCommentScope>
        get() = emptySet()

    /**
     * All the scopes whose suppression quick fix should target the _parent/outer_ PsiElement.
     *
     * This is useful in the cases like `\ref{...}`, where if you want to supress for group, you do not want the
     * quick fix to result in `\ref{ %! Suppress = ... }`, but rather to target the group in which the
     * `\ref` is contained (if it exists).
     */
    open val outerSuppressionScopes: Set<MagicCommentScope>
        get() = emptySet()

    /**
     * Whether this inspection should be activated under the given contexts.
     */
    protected fun isApplicableInContexts(contexts: LContextSet): Boolean {
        val applicableContexts = this.applicableContexts
        if (applicableContexts != null) {
            // Only inspect if at least one of the applicable contexts is present, namely the intersection is non-empty.
            if(!applicableContexts.existsIntersection(contexts)) return false
        }
        val excludedContexts = this.excludedContexts
        return excludedContexts.isEmpty() || !contexts.any { it in excludedContexts }
    }

    /**
     * Inspects a single element, given the contexts it is in.
     * It is the caller's responsibility to check the context via [isApplicableInContexts].
     *
     * @param isOnTheFly Whether the inspection is run on-the-fly (in the editor) or in batch mode (code inspection).
     */
    abstract fun inspectElement(
        element: PsiElement, contexts: LContextSet,
        bundle: DefinitionBundle, file: PsiFile,
        manager: InspectionManager, isOnTheFly: Boolean,
        descriptors: MutableList<ProblemDescriptor>
    )

    override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
        element ?: return SuppressQuickFix.EMPTY_ARRAY
        val result = ArrayList<SuppressionFixBase>()
        element.let { elt ->
            elt.containingFile?.let { result.add(FileSuppressionFix(it.createSmartPointer())) }

            elt.suppressionElement<LatexEnvironment>(MagicCommentScope.ENVIRONMENT, outerSuppressionScopes)?.let {
                result.add(EnvironmentSuppressionFix(it))
            }
            elt.suppressionElement<LatexMathEnvironment>(MagicCommentScope.MATH_ENVIRONMENT, outerSuppressionScopes)
                ?.let {
                    result.add(MathEnvironmentSuppressionFix(it))
                }
            elt.suppressionElement<LatexCommands>(MagicCommentScope.COMMAND, outerSuppressionScopes)?.let {
                result.add(CommandSuppressionFix(it))
            }
            elt.suppressionElement<LatexGroup>(MagicCommentScope.GROUP, outerSuppressionScopes)?.let {
                result.add(GroupSuppressionFix(it))
            }
        }

        return result.filter { it.suppressionScope !in ignoredSuppressionScopes }.toTypedArray()
    }

    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return performanceTracker.track {
            // Only inspect the right file types.
            if (file.fileType !in inspectionGroup.fileTypes) {
                return@track null
            }
            if (isFileSuppressed(file)) return@track null

            val defBundle = LatexDefinitionService.getInstance(file.project).getDefBundlesMerged(file)
            if (!isFileApplicable(file, defBundle)) return@track null

            val traverser = InspectionTraverser(
                manager, isOnTheFly, defBundle, file, LatexContexts.baseContexts
            )
            val result = traverser.doInspect(file)
            if (result.isEmpty()) ProblemDescriptor.EMPTY_ARRAY else result.toTypedArray()
        }
    }

    /**
     * Make a quick check to see if the file is applicable for this inspection.
     *
     * If you do not need [bundle] to make this decision, prefer overriding [isAvailableForFile] instead.
     *
     * @see isAvailableForFile
     */
    protected open fun isFileApplicable(file: PsiFile, bundle: DefinitionBundle): Boolean {
        return true
    }

    /**
     * Looks at the start of the file to see if there is a magic comment that suppresses this inspection.
     */
    protected fun isFileSuppressed(file: PsiFile): Boolean {
        val content = file.findFirstChildTyped<LatexContent>() ?: return true // Empty file, nothing to inspect.
        for (e in content.traverse(4)) {
            if (e is LatexContent || e is LatexNoMathContent || e is PsiWhiteSpace || e is PsiComment) continue
            if (e.elementType == LatexTypes.MAGIC_COMMENT_TOKEN) continue
            if (e !is LatexMagicComment) break
            if (e.containsKeyValuePair("suppress", inspectionId)) return true
        }
        return false
    }

    /**
     * Decides whether the children of the given element should be inspected.
     *
     * Overriding this method can be used to avoid descending into certain elements to improve performance.
     */
    protected open fun shouldInspectChildrenOf(element: PsiElement, state: LContextSet, lookup: LatexSemanticsLookup): Boolean {
        return true
    }

    protected inner class InspectionTraverser(
        private val manager: InspectionManager, private val isOnTheFly: Boolean,
        val bundle: DefinitionBundle, val file: PsiFile,
        baseContexts: LContextSet
    ) : LatexWithContextTraverser(baseContexts, bundle) {

        private val descriptors: MutableList<ProblemDescriptor> = SmartList()

        private var isSuppressedNext: Boolean = false

        override fun enterContextIntro(intro: LatexContextIntro): WalkAction {
            if(intro.introducesAny(skipChildrenInContext)) {
                return WalkAction.SKIP_CHILDREN
            }
            return super.enterContextIntro(intro)
        }

        override fun elementStart(e: PsiElement): WalkAction {
            if (e is LatexMagicComment) {
                if (e.containsKeyValuePair("suppress", inspectionId)) {
                    isSuppressedNext = true
                }
                return WalkAction.SKIP_CHILDREN
            }
            if (e is PsiComment || e is PsiWhiteSpace) return WalkAction.SKIP_CHILDREN
            if (isSuppressedNext) {
                // Do not inspect this element, it is suppressed previously by a magic comment.
                isSuppressedNext = false
                return WalkAction.SKIP_CHILDREN
            }

            inspectElement(e, state, bundle, file, manager, isOnTheFly, descriptors)
            return if (shouldInspectChildrenOf(e, state, lookup)) WalkAction.CONTINUE else WalkAction.SKIP_CHILDREN
        }

        fun doInspect(file: PsiFile): List<ProblemDescriptor> {
            traverseRecur(file)
            return descriptors
        }
    }

    companion object {
        val performanceTracker = SimplePerformanceTracker()
    }

    /**
     * @author Hannah Schellekens
     */
    protected abstract inner class SuppressionFixBase(val targetElement: SmartPsiElementPointer<out PsiElement>) :
        SuppressQuickFix {

        /**
         * The scope to which to apply the suppression.
         */
        abstract val suppressionScope: MagicCommentScope

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val magicComment = MutableMagicComment<String, String>()
            magicComment.addValue(DefaultMagicKeys.SUPPRESS, inspectionId)
            targetElement.element?.addMagicCommentToPsiElement(magicComment)
        }

        /**
         * There is no use for a preview for suppression "fixes" (there also is no preview when suppressing Java and Kotlin inspections),
         * disable it manually to avoid [Field blocks intention preview](https://www.jetbrains.com/help/inspectopedia/ActionIsNotPreviewFriendly.html) warnings.
         */
        override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo =
            IntentionPreviewInfo.EMPTY

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return context.containingFile.fileType == LatexFileType
        }

        override fun isSuppressAll() = false
    }

    /**
     * @author Hannah Schellekens
     */
    protected inner class FileSuppressionFix(val file: SmartPsiElementPointer<PsiFile>) : SuppressionFixBase(file) {

        override val suppressionScope = MagicCommentScope.FILE

        override fun getFamilyName() = "Suppress for file '${file.element?.name}'"
    }

    /**
     * @author Hannah Schellekens
     */
    protected inner class EnvironmentSuppressionFix(parentEnvironment: LatexEnvironment) :
        SuppressionFixBase(parentEnvironment.createSmartPointer()) {

        /**
         * The name of the environment to suppress, or `null` when there is no environment name available.
         */
        private val environmentName: String = parentEnvironment.getEnvironmentName()

        override val suppressionScope = MagicCommentScope.ENVIRONMENT

        override fun getFamilyName() = "Suppress for environment '$environmentName'"

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return super.isAvailable(project, context)
        }
    }

    /**
     * @author Hannah Schellekens
     */
    protected inner class MathEnvironmentSuppressionFix(parentMathEnvironment: LatexMathEnvironment) :
        SuppressionFixBase(parentMathEnvironment.createSmartPointer()) {

        override val suppressionScope = MagicCommentScope.MATH_ENVIRONMENT

        override fun getFamilyName() = "Suppress for math environment"
    }

    /**
     * @author Hannah Schellekens
     */
    protected inner class CommandSuppressionFix(parentCommand: LatexCommands) :
        SuppressionFixBase(parentCommand.createSmartPointer()) {

        /**
         * The name of the command to suppress, or `null` when there is no command name available.
         */
        private val commandToken: String? = parentCommand.commandToken.text

        override val suppressionScope = MagicCommentScope.COMMAND

        override fun getFamilyName() = "Suppress for command '$commandToken'"

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return commandToken != null && super.isAvailable(project, context)
        }
    }

    /**
     * @author Hannah Schellekens
     */
    protected inner class GroupSuppressionFix(parentGroup: LatexGroup) :
        SuppressionFixBase(parentGroup.createSmartPointer()) {

        override val suppressionScope = MagicCommentScope.GROUP

        override fun getFamilyName() = "Suppress for group"
    }
}