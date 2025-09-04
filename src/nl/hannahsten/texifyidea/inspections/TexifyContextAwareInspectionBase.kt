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
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.util.SmartList
import nl.hannahsten.texifyidea.action.debug.SimplePerformanceTracker
import nl.hannahsten.texifyidea.file.LatexFileType
import nl.hannahsten.texifyidea.index.LatexDefinitionService
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase.Companion.suppressionElement
import nl.hannahsten.texifyidea.lang.LContextSet
import nl.hannahsten.texifyidea.lang.LatexContexts
import nl.hannahsten.texifyidea.lang.LatexSemanticsLookup
import nl.hannahsten.texifyidea.lang.magic.DefaultMagicKeys
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.lang.magic.MutableMagicComment
import nl.hannahsten.texifyidea.lang.magic.addMagicCommentToPsiElement
import nl.hannahsten.texifyidea.lang.magic.containsPair
import nl.hannahsten.texifyidea.lang.magic.magicComment
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexContent
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexGroup
import nl.hannahsten.texifyidea.psi.LatexMagicComment
import nl.hannahsten.texifyidea.psi.LatexMathEnvironment
import nl.hannahsten.texifyidea.psi.LatexNoMathContent
import nl.hannahsten.texifyidea.psi.LatexWithContextTraverser
import nl.hannahsten.texifyidea.psi.getEnvironmentName
import nl.hannahsten.texifyidea.util.parser.findFirstChildTyped
import nl.hannahsten.texifyidea.util.parser.traverse
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

abstract class TexifyContextAwareInspectionBase(
    /**
     * The inspectionGroup the inspection falls under.
     */
    val inspectionGroup: InsightGroup,
    /**
     * A unique string identifier for the inspection.
     */
    val inspectionId: String
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
     * Inspects a single element, given the contexts it is in.
     */
    abstract fun inspectElement(
        element: PsiElement, contexts: LContextSet,
        manager: InspectionManager, isOnTheFly: Boolean,
        descriptors: MutableList<ProblemDescriptor>
    )

    override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
        val result = ArrayList<SuppressionFixBase>()

        element?.let { elt ->
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
        // Only inspect the right file types.
        if (file.fileType !in inspectionGroup.fileTypes) {
            return null
        }
        if (!isFileApplicable(file)) return null
        if (isFileSuppressed(file)) return null

        val lookup = LatexDefinitionService.getInstance(file.project).getDefBundlesMerged(file)
        val traverser = InspectionTraverser(
            manager, isOnTheFly, lookup, LatexContexts.baseContexts
        )
        val result = traverser.doInspect(file)
        return if (result.isEmpty()) ProblemDescriptor.EMPTY_ARRAY else result.toTypedArray()
    }

    protected open fun isFileApplicable(file: PsiFile): Boolean {
        return true
    }

    protected fun isFileSuppressed(file: PsiFile): Boolean {
        val content = file.findFirstChildTyped<LatexContent>() ?: return true // Empty file, nothing to inspect.
        for (e in content.traverse(4)) {
            if (e is LatexNoMathContent) continue
            if (e !is LatexMagicComment) break
            e.magicComment()?.let {
                if (it.containsPair("suppress", inspectionId)) return true
            }
        }
        return false
    }

    protected inner class InspectionTraverser(
        private val manager: InspectionManager, private val isOnTheFly: Boolean,
        lookup: LatexSemanticsLookup, baseContexts: LContextSet
    ) : LatexWithContextTraverser(baseContexts, lookup) {

        private val descriptors: MutableList<ProblemDescriptor> = SmartList()

        private var isSuppressedNext: Boolean = false

        override fun elementStart(e: PsiElement): WalkAction {
            if (e is LatexMagicComment) {
                if (e.magicComment()?.containsPair("suppress", inspectionId) == true) {
                    isSuppressedNext = true
                }
                return WalkAction.SKIP_CHILDREN
            }
            if (e is PsiComment) return WalkAction.SKIP_CHILDREN
            if (LatexContexts.Comment in state) return WalkAction.SKIP_CHILDREN
            if (isSuppressedNext) {
                // Do not inspect this element, it is suppressed previously by a magic comment.
                isSuppressedNext = false
                return WalkAction.SKIP_CHILDREN
            }

            inspectElement(e, state, manager, isOnTheFly, descriptors)
            return WalkAction.CONTINUE
        }

        fun doInspect(file: PsiFile): List<ProblemDescriptor> {
            traverseRecur(file)
            return descriptors
        }
    }

    object PerformanceTracker : SimplePerformanceTracker {
        override val countOfBuilds: AtomicInteger = AtomicInteger(0)
        override val totalTimeCost: AtomicLong = AtomicLong(0)
    }

    /**
     * @author Hannah Schellekens
     */
    private abstract inner class SuppressionFixBase(val targetElement: SmartPsiElementPointer<out PsiElement>) :
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
    private inner class FileSuppressionFix(val file: SmartPsiElementPointer<PsiFile>) : SuppressionFixBase(file) {

        override val suppressionScope = MagicCommentScope.FILE

        override fun getFamilyName() = "Suppress for file '${file.element?.name}'"
    }

    /**
     * @author Hannah Schellekens
     */
    private inner class EnvironmentSuppressionFix(parentEnvironment: LatexEnvironment) :
        SuppressionFixBase(parentEnvironment.createSmartPointer()) {

        /**
         * The name of the environment to suppress, or `null` when there is no environment name available.
         */
        private val environmentName: String? = parentEnvironment.getEnvironmentName()

        override val suppressionScope = MagicCommentScope.ENVIRONMENT

        override fun getFamilyName() = "Suppress for environment '$environmentName'"

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return environmentName != null && super.isAvailable(project, context)
        }
    }

    /**
     * @author Hannah Schellekens
     */
    private inner class MathEnvironmentSuppressionFix(parentMathEnvironment: LatexMathEnvironment) :
        SuppressionFixBase(parentMathEnvironment.createSmartPointer()) {

        override val suppressionScope = MagicCommentScope.MATH_ENVIRONMENT

        override fun getFamilyName() = "Suppress for math environment"
    }

    /**
     * @author Hannah Schellekens
     */
    private inner class CommandSuppressionFix(parentCommand: LatexCommands) :
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
    private inner class GroupSuppressionFix(parentGroup: LatexGroup) :
        SuppressionFixBase(parentGroup.createSmartPointer()) {

        override val suppressionScope = MagicCommentScope.GROUP

        override fun getFamilyName() = "Suppress for group"
    }
}