package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.SmartList
import nl.rubensten.texifyidea.file.LatexFileType
import nl.rubensten.texifyidea.insight.InsightGroup
import nl.rubensten.texifyidea.lang.magic.*
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.psi.LatexEnvironment
import nl.rubensten.texifyidea.psi.LatexGroup
import nl.rubensten.texifyidea.psi.LatexMathEnvironment
import nl.rubensten.texifyidea.util.isComment
import nl.rubensten.texifyidea.util.name
import nl.rubensten.texifyidea.util.parentOfType

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
     * The magic comment scopes that should not have a [SuppressQuickFix].
     */
    open val ignoredSuppressionScopes: Set<MagicCommentScope> = emptySet()

    /**
     * All the scopes whose suppression quick fix should target the _parent/outer_ PsiElement.
     *
     * This is useful in the cases like `\ref{...}`, where if you want to supress for group, you do not want the
     * quick fix to result in `\ref{ %! Suppress = ... }`, but rather to target the group in which the
     * `\ref` is contained (if it exists).
     */
    open val outerSuppressionScopes: Set<MagicCommentScope> = emptySet()

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
        return magicComment()?.containsPair("suppress", inspectionId) == true ||
                allParentMagicComments().containsPair("suppress", inspectionId)
    }

    override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
        val result = ArrayList<SuppressionFixBase>()

        element?.let { elt ->
            elt.containingFile?.let { result.add(FileSuppressionFix(it)) }

            elt.suppressionElement<LatexEnvironment>(MagicCommentScope.ENVIRONMENT)?.let {
                result.add(EnvironmentSuppressionFix(it))
            }
            elt.suppressionElement<LatexMathEnvironment>(MagicCommentScope.MATH_ENVIRONMENT)?.let {
                result.add(MathEnvironmentSuppressionFix(it))
            }
            elt.suppressionElement<LatexCommands>(MagicCommentScope.COMMAND)?.let {
                result.add(CommandSuppressionFix(it))
            }
            elt.suppressionElement<LatexGroup>(MagicCommentScope.GROUP)?.let {
                result.add(GroupSuppressionFix(it))
            }
        }

        return result.filter { it.suppressionScope !in ignoredSuppressionScopes }.toTypedArray()
    }

    /**
     * Get the element relative to `this` element that must be targeted by the suppression quick fix given the
     * magic comment scope.
     */
    private inline fun <reified Psi : PsiElement> PsiElement.suppressionElement(scope: MagicCommentScope): Psi? {
        val parent = parentOfType(Psi::class)

        return if (scope in outerSuppressionScopes) {
            parent?.parentOfType(Psi::class)
        }
        else parent
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

    /**
     * @author Ruben Schellekens
     */
    private abstract inner class SuppressionFixBase(val targetElement: PsiElement) : SuppressQuickFix {

        /**
         * The scope to which to apply the suppression.
         */
        abstract val suppressionScope: MagicCommentScope

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val magicComment = MutableMagicComment<String, String>()
            magicComment.addValue(DefaultMagicKeys.SUPPRESS, inspectionId)
            targetElement.addMagicCommentToPsiElement(magicComment)
        }

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return context.containingFile.fileType == LatexFileType
        }

        override fun isSuppressAll() = false
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class FileSuppressionFix(val file: PsiFile) : SuppressionFixBase(file) {

        override val suppressionScope = MagicCommentScope.FILE

        override fun getFamilyName() = "Suppress for file '${file.name}'"
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class EnvironmentSuppressionFix(parentEnvironment: LatexEnvironment) : SuppressionFixBase(parentEnvironment) {

        /**
         * The name of the environment to suppress, or `null` when there is no environment name available.
         */
        private val environmentName: String? = parentEnvironment.name()?.text

        override val suppressionScope = MagicCommentScope.ENVIRONMENT

        override fun getFamilyName() = "Suppress for environment '$environmentName'"

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return environmentName != null && super.isAvailable(project, context)
        }
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class MathEnvironmentSuppressionFix(parentMathEnvironment: LatexMathEnvironment) : SuppressionFixBase(parentMathEnvironment) {

        override val suppressionScope = MagicCommentScope.MATH_ENVIRONMENT

        override fun getFamilyName() = "Suppress for math environment"
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class CommandSuppressionFix(parentCommand: LatexCommands) : SuppressionFixBase(parentCommand) {

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
     * @author Ruben Schellekens
     */
    private inner class GroupSuppressionFix(val parentGroup: LatexGroup) : SuppressionFixBase(parentGroup) {

        override val suppressionScope = MagicCommentScope.GROUP

        override fun getFamilyName() = "Suppress for group"
    }
}