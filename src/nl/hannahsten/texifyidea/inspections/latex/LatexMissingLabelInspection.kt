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
import nl.hannahsten.texifyidea.psi.LatexEnvironment
import nl.hannahsten.texifyidea.util.*
import nl.hannahsten.texifyidea.util.files.commandsInFile
import nl.hannahsten.texifyidea.util.files.environmentsInFile
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

        val environments = file.environmentsInFile()
        for (environment in environments) {
            if (!Magic.Environment.labeled.containsKey(environment.environmentName) || environment.environmentName == "lstlisting") {
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
        if (environment.hasLabel()) {
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
     * @author Hannah Schellekens
     */
    private class InsertLabelAfterCommandFix : LocalQuickFix {

        override fun getFamilyName() = "Insert label"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexCommands

            val factory = LatexPsiFactory(project)
            val labelCommand = factory.createUniqueLabelCommandFor(command) ?: return

            // Insert label
            // command -> NoMathContent -> Content -> Container containing the command
            val commandContent = command.parent.parent
            commandContent.parent.addAfter(labelCommand, commandContent)

            // Adjust caret offset.
            val editor = command.containingFile.openedEditor() ?: return
            val caret = editor.caretModel
            caret.moveToOffset(labelCommand.endOffset())
        }
    }

    private class InsertLabelInEnvironmentFix : LocalQuickFix {
        override fun getFamilyName() = "Insert label"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val command = descriptor.psiElement as LatexEnvironment
            val factory = LatexPsiFactory(project)
            val labelCommand = factory.createUniqueLabelCommandFor(command) ?: return
            if (command.environmentContent == null) {
                command.addAfter(factory.createEnvironmentContent(), command.beginCommand)
            }

            val environmentContent = command.environmentContent!!

            // in a float environment the label must be inserted after a caption
            val caption = environmentContent.childrenOfType<LatexCommands>().findLast { c -> c.name == "\\caption" }
            if (caption != null) {
                environmentContent.addAfter(labelCommand, caption.parent.parent)
            }
            else {
                environmentContent.add(labelCommand)
            }

            // Adjust caret offset
            val openedEditor = command.containingFile.openedEditor() ?: return
            val caretModel = openedEditor.caretModel
            caretModel.moveToOffset(labelCommand.endOffset())
        }

    }
}