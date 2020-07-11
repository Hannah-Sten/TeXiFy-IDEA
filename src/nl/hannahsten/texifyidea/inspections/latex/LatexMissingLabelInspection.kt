package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.intentions.LatexAddLabelIntention
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.psi.LatexPsiHelper
import nl.hannahsten.texifyidea.settings.TexifyConfigurable
import nl.hannahsten.texifyidea.settings.TexifySettings
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.environmentsInFile
import nl.hannahsten.texifyidea.util.files.openedEditor
import org.jetbrains.annotations.Nls
import java.util.*

/**
 * Currently only works for Chapters, Sections and Subsections.
 *
 * @author Hannah Schellekens
 */
open class LatexMissingLabelInspection : TexifyInspectionBase() {

    override val inspectionGroup = InsightGroup.LATEX

    override val inspectionId = "MissingLabel"

    override val ignoredSuppressionScopes = EnumSet.of(MagicCommentScope.GROUP)!!

    override fun getDisplayName() = "Missing labels"

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = descriptorList()

        val minimumLevel = Magic.Command.labeledLevels[TexifySettings.getInstance().missingLabelMinimumLevel] ?: error("No valid minimum level given")
        val labeledCommands = Magic.Command.labeledLevels.keys.filter { command ->
            Magic.Command.labeledLevels[command]?.let { it <= minimumLevel } == true // -1 is a higher level than 0
        }.map { "\\" + it.command }
        file.commandsInFile().filter {
            labeledCommands.contains(it.name) && it.name != "\\item" && !it.hasStar()
        }.forEach { addCommandDescriptor(it, descriptors, manager, isOntheFly) }

        file.environmentsInFile().filter { Magic.Environment.labeled.containsKey(it.environmentName) }
            .forEach { addEnvironmentDescriptor(it, descriptors, manager, isOntheFly) }

        return descriptors
    }

    /**
     * Adds a command descriptor to the given command if there is a label missing.
     *
     * @return `true` when a descriptor was added, or `false` when no descriptor was added.
     */
    private fun addCommandDescriptor(command: LatexCommands, descriptors: MutableList<ProblemDescriptor>,
                                     manager: InspectionManager, isOntheFly: Boolean): Boolean {
        if (command.hasLabel()) {
            return false
        }

        // For adding the label, see LatexAddLabelIntention
        descriptors.add(manager.createProblemDescriptor(
                command,
                "Missing label",
                arrayOf(InsertLabelAfterCommandFix(), ChangeMinimumLabelLevelFix()),
                ProblemHighlightType.WEAK_WARNING,
                isOntheFly,
                false
        ))

        return true
    }

    private fun addEnvironmentDescriptor(environment: LatexEnvironment, descriptors: MutableList<ProblemDescriptor>,
                                         manager: InspectionManager, isOntheFly: Boolean): Boolean {
        if (environment.label != null) {
            return false
        }

        descriptors.add(manager.createProblemDescriptor(
                environment,
                "Missing label",
                arrayOf(InsertLabelInEnvironmentFix()),
                ProblemHighlightType.WEAK_WARNING,
                isOntheFly,
                false
        ))

        return true
    }

    /**
     * Open the settings page so the user can change the minimum labeled level.
     */
    private class ChangeMinimumLabelLevelFix : LocalQuickFix {
        @Nls
        override fun getFamilyName(): String {
            return "Change minimum sectioning level"
        }

        override fun startInWriteAction() = false

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, TexifyConfigurable::class.java)
        }
    }

    abstract class LabelQuickFix : LocalQuickFix {
        protected fun getUniqueLabelName(base: String, prefix: String?, file: PsiFile): String {
            val labelBase = "$prefix:$base"
            val allLabels = file.findLatexAndBibtexLabelStringsInFileSet()
            return appendCounter(labelBase, allLabels)
        }

        /**
         * Keeps adding a counter behind the label until there is no other label with that name.
         */
        private fun appendCounter(label: String, allLabels: Set<String>): String {
            var counter = 2
            var candidate = label

            while (allLabels.contains(candidate)) {
                candidate = label + (counter++)
            }

            return candidate
        }
    }

    /**
     * This is also an intention, but in order to keep the same alt+enter+enter functionality (because we have an other
     * quickfix as well) we keep it as a quickfix also.
     */
    private class InsertLabelAfterCommandFix : LabelQuickFix() {

        // It has to appear in alphabetical order before the other quickfix
        override fun getFamilyName() = "Add label for this command"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            LatexAddLabelIntention().invoke(project, command.containingFile.openedEditor(), command.containingFile)
        }
    }

    private class InsertLabelInEnvironmentFix : LabelQuickFix() {
        override fun getFamilyName() = "Add label for this environment"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexEnvironment
            val helper = LatexPsiHelper(project)
            // Determine label name.
            val createdLabel = getUniqueLabelName(command.environmentName.formatAsLabel(),
                    Magic.Environment.labeled[command.environmentName], command.containingFile)

            val moveCaretAfter: PsiElement
            moveCaretAfter = if (Magic.Environment.labelAsParameter.contains(command.environmentName)) {
                val insertedElements = helper.addOptionalParameter(command.beginCommand, "label", createdLabel)
                insertedElements.last()
            }
            else {
                // in a float environment the label must be inserted after a caption
                val labelCommand = helper.addToContent(command, helper.createLabelCommand(createdLabel),
                        command.environmentContent?.childrenOfType<LatexCommands>()
                                ?.findLast { c -> c.name == "\\caption" })
                labelCommand
            }

            // Adjust caret offset
            val openedEditor = command.containingFile.openedEditor() ?: return
            val caretModel = openedEditor.caretModel
            caretModel.moveToOffset(moveCaretAfter.endOffset())
        }
    }
}