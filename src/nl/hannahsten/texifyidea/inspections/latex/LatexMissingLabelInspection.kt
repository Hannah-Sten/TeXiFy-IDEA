package nl.hannahsten.texifyidea.inspections.latex

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.hannahsten.texifyidea.insight.InsightGroup
import nl.hannahsten.texifyidea.inspections.TexifyInspectionBase
import nl.hannahsten.texifyidea.lang.magic.MagicCommentScope
import nl.hannahsten.texifyidea.psi.LatexCommands
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.document
import nl.hannahsten.texifyidea.util.files.openedEditor
import java.util.*

/**
 * Currently only works for Chapters, Sections and Subsections.
 *
 * Planned is to also implement this for other environments.
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
                InsertLabelFix(),
                ProblemHighlightType.WEAK_WARNING,
                isOntheFly
        ))

        return true
    }

    /**
     * @author Hannah Schellekens
     */
    private class InsertLabelFix : LocalQuickFix {

        override fun getFamilyName() = "Insert label"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val required = command.requiredParameters
            if (required.isEmpty()) {
                return
            }

            // Determine label name.
            val prefix = Magic.Command.labeled[command.name]
            val labelName = required[0].formatAsLabel()
            val createdLabelBase = "$prefix:$labelName"

            val allLabels = file.findLabelsInFileSet()
            val createdLabel = appendCounter(createdLabelBase, allLabels)

            // Insert label.
            val labelText = "\\label{$createdLabel}"
            document.insertString(command.endOffset(), labelText)

            // Adjust caret offset.
            val editor = file.openedEditor() ?: return
            val caret = editor.caretModel
            caret.moveToOffset(command.endOffset() + labelText.length)
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
}