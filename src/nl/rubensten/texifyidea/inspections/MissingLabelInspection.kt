package nl.rubensten.texifyidea.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import nl.rubensten.texifyidea.psi.LatexCommands
import nl.rubensten.texifyidea.util.*
import kotlin.reflect.jvm.internal.impl.utils.SmartList

/**
 * Currently only works for Chapters, Sections and Subsections.
 *
 * Planned is to also implement this for other environments.
 *
 * @author Ruben Schellekens
 */
open class MissingLabelInspection : TexifyInspectionBase() {

    override fun getDisplayName(): String {
        return "Missing labels"
    }

    override fun getShortName(): String {
        return "MissingLabel"
    }

    override fun inspectFile(file: PsiFile, manager: InspectionManager, isOntheFly: Boolean): List<ProblemDescriptor> {
        val descriptors = SmartList<ProblemDescriptor>()

        val commands = file.commandsInFile()
        for (cmd in commands) {
            if (!LabelConventionInspection.LABELED_COMMANDS.containsKey(cmd.name) || cmd.name == "\\item") {
                continue
            }

            addCommandDescriptor(cmd, descriptors, manager, isOntheFly)
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
     * @author Ruben Schellekens
     */
    private class InsertLabelFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return "Insert label"
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands
            val file = command.containingFile
            val document = file.document() ?: return
            val required = command.requiredParameters
            if (required.isEmpty()) {
                return
            }

            // Determine label name.
            val prefix = LabelConventionInspection.LABELED_COMMANDS[command.name]
            val labelName = required[0].camelCase()
            val createdLabelBase = "$prefix:$labelName"

            val allLabels = TexifyUtil.findLabelsInFileSet(file)
            val createdLabel = appendCounter(createdLabelBase, allLabels)

            // Insert label.
            document.insertString(command.endOffset(), "\\label{$createdLabel}")
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