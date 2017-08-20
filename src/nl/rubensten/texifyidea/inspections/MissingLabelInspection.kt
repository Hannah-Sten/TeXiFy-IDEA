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
 * Planned is to also implement this for other environment.
 *
 * @author Ruben Schellekens
 */
open class MissingLabelInspection : TexifyInspectionBase() {

    companion object {

        /**
         * Map that maps all commands that are expected to have a label to the label prefix they have by convention.
         *
         * command name `=>` label prefix without colon
         */
        private val LABELED_COMMANDS = mapOf(
                Pair("\\chapter", "ch"),
                Pair("\\section", "sec"),
                Pair("\\subsection", "subsec")
        )
    }

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
            if (!LABELED_COMMANDS.containsKey(cmd.name)) {
                continue
            }

            if (!cmd.hasLabel()) {
                descriptors.add(manager.createProblemDescriptor(
                        cmd,
                        "Missing label",
                        InsertLabelFix(),
                        ProblemHighlightType.WEAK_WARNING,
                        isOntheFly
                ))
            }
        }

        return descriptors
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
            val prefix = LABELED_COMMANDS[command.name]
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