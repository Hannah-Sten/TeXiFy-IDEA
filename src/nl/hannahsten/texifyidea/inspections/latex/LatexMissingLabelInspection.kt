package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.environmentsInFile
import nl.hannahsten.texifyidea.util.files.openedEditor
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

        val commands = file.commandsInFile()
        for (command in commands) {
            if (!Magic.Command.labeled.containsKey(command.name) || command.name == "\\item" || command.hasStar()) {
                continue
            }

            addCommandDescriptor(command, descriptors, manager, isOntheFly)
        }

        val environments = file.environmentsInFile()
        for (environment in environments) {
            if (!Magic.Environment.labeled.containsKey(environment.environmentName)) {
                continue
            }

            addEnvironmentDescriptor(environment, descriptors, manager, isOntheFly)
        }

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

        descriptors.add(manager.createProblemDescriptor(
                command,
                "Missing label",
                InsertLabelAfterCommandFix(),
                ProblemHighlightType.WEAK_WARNING,
                isOntheFly
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

    abstract class LabelQuickFix : LocalQuickFix {
        protected fun getUniqueLabelName(base: String, prefix: String?, file: PsiFile): String {
            val labelBase = "$prefix:$base"
            val allLabels = file.findLabelsInFileSet()
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
     * @author Hannah Schellekens
     */
    private class InsertLabelAfterCommandFix : LabelQuickFix() {

        override fun getFamilyName() = "Insert label"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands

            // Determine label name.
            val required = command.requiredParameters
            if (required.isEmpty()) {
                return
            }

            val createdLabel = getUniqueLabelName(required[0].formatAsLabel(),
                    Magic.Command.labeled[command.name!!], command.containingFile)

            val factory = LatexPsiHelper(project)

            // Insert label
            // command -> NoMathContent -> Content -> Container containing the command
            val commandContent = command.parent.parent
            val labelCommand = commandContent.parent.addAfter(factory.createLabelCommand(createdLabel), commandContent)

            // Adjust caret offset.
            val editor = command.containingFile.openedEditor() ?: return
            val caret = editor.caretModel
            caret.moveToOffset(labelCommand.endOffset())
        }
    }

    private class InsertLabelInEnvironmentFix : LabelQuickFix() {
        override fun getFamilyName() = "Insert label"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexEnvironment
            val helper = LatexPsiHelper(project)
            // Determine label name.
            val createdLabel = getUniqueLabelName(command.environmentName.formatAsLabel(),
                    Magic.Environment.labeled[command.environmentName], command.containingFile)


            val moveCaretAfter: PsiElement;
            if (Magic.Environment.labelAsParameter.contains(command.environmentName)) {
                val insertedElements = helper.addOptionalParameter(command.beginCommand, "label", createdLabel)
                moveCaretAfter = insertedElements.last()
            }
            else {
                // in a float environment the label must be inserted after a caption
                val labelCommand = helper.addToContent(command, helper.createLabelCommand(createdLabel),
                        command.environmentContent?.childrenOfType<LatexCommands>()
                                ?.findLast { c -> c.name == "\\caption" })
                moveCaretAfter = labelCommand
            }

            // Adjust caret offset
            val openedEditor = command.containingFile.openedEditor() ?: return
            val caretModel = openedEditor.caretModel
            caretModel.moveToOffset(moveCaretAfter.endOffset())
        }

    }
}