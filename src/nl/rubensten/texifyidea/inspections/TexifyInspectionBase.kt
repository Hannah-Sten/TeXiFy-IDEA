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

    override fun getBatchSuppressActions(element: PsiElement?): Array<SuppressQuickFix> {
        val result = ArrayList<SuppressQuickFix>()

        element?.let { elt ->
            elt.containingFile?.let { result.add(FileSuppressionFix(it)) }
            elt.parentOfType(LatexEnvironment::class)?.let { result.add(EnvironmentSuppressionFix(it)) }
            elt.parentOfType(LatexCommands::class)?.let { result.add(CommandSuppressionFix(it)) }
            elt.parentOfType(LatexGroup::class)?.let { result.add(GroupSuppressionFix(it)) }
        }

        return result.toTypedArray()
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
    private abstract inner class SuppressionFixBase : SuppressQuickFix {

        /**
         * The scope to which to apply the suppression.
         */
        protected abstract val suppressionScope: MagicCommentScope

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val magicComment = MutableMagicComment<String, String>()
            magicComment.addValue(DefaultMagicKeys.SUPPRESS, inspectionId)
            descriptor.psiElement?.addMagicComment(magicComment, suppressionScope)
        }

        override fun isAvailable(project: Project, context: PsiElement): Boolean {
            return context.containingFile.fileType == LatexFileType
        }

        override fun isSuppressAll() = false
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class FileSuppressionFix(val file: PsiFile) : SuppressionFixBase() {

        override val suppressionScope = MagicCommentScope.FILE

        override fun getFamilyName() = "Suppress for file '${file.name}'"
    }

    /**
     * @author Ruben Schellekens
     */
    private inner class EnvironmentSuppressionFix(val parentEnvironment: LatexEnvironment) : SuppressionFixBase() {

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
    private inner class CommandSuppressionFix(val parentCommand: LatexCommands) : SuppressionFixBase() {

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
    private inner class GroupSuppressionFix(val parentGroup: LatexGroup) : SuppressionFixBase() {

        override val suppressionScope = MagicCommentScope.GROUP

        override fun getFamilyName() = "Suppress for group"
    }
}